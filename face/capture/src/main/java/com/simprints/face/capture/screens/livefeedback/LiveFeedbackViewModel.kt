package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.extentions.area
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.models.FaceTarget
import com.simprints.face.capture.models.SymmetricTarget
import com.simprints.face.capture.usecases.GetSpoofCheckConfigurationUseCase
import com.simprints.face.capture.usecases.IsUsingAutoCaptureUseCase
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FaceConfiguration.SpoofCheckConfiguration
import com.simprints.infra.config.store.models.ModalitySdkType
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTimedValue

@HiltViewModel
internal class LiveFeedbackViewModel @Inject constructor(
    private val resolveFaceBioSdk: ResolveFaceBioSdkUseCase,
    private val configRepository: ConfigRepository,
    private val eventReporter: SimpleCaptureEventReporter,
    private val timeHelper: TimeHelper,
    private val isUsingAutoCaptureUseCase: IsUsingAutoCaptureUseCase,
    private val getSpoofCheckConfiguration: GetSpoofCheckConfigurationUseCase,
    @param:DispatcherBG private val bgDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private var attemptNumber: Int = 1
    private var samplesToCapture: Int = 1
    private var qualityThreshold: Float = 0f

    private val faceTarget = FaceTarget(
        SymmetricTarget(VALID_YAW_DELTA),
        SymmetricTarget(VALID_ROLL_DELTA),
        0.20f..0.5f,
    )
    private val fallbackCaptureEventStartTime = timeHelper.now()
    private var shouldSendFallbackCaptureEvent: AtomicBoolean = AtomicBoolean(true)
    private var fallbackCapture: FaceDetection? = null

    val userCaptures = mutableListOf<FaceDetection>()
    var sortedQualifyingCaptures = listOf<FaceDetection>()

    /**
     * The single source of truth for the whole screen.
     * The fragment renders this deterministically; all transitions funnel through [emit].
     */
    private val _state = MutableStateFlow(LiveFeedbackState.initial())
    val state: StateFlow<LiveFeedbackState> = _state.asStateFlow()

    var isAutoCapture: Boolean = false
    private var spoofCheckConfig: SpoofCheckConfiguration = SpoofCheckConfiguration.DISABLED
    private var spoofCheckCount: Int = 0

    private var captureImagingStartTime: Long = 0
    private var validationStartTime: Long = 0
    private var isAutoCaptureHeldOff = true
    private var autoCaptureImagingTimeoutJob: Job? = null
    private var autoCaptureImagingDurationMillis: Long = FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT
    private lateinit var faceDetector: FaceDetector

    private val phase: LiveFeedbackState.Phase
        get() = _state.value.phase

    private fun emit(
        phase: LiveFeedbackState.Phase = _state.value.phase,
        feedback: LiveFeedbackState.Feedback = _state.value.feedback,
        detectionForTint: FaceDetection? = null,
        result: List<FaceDetection> = _state.value.result,
    ) {
        _state.update { currentState ->
            currentState.copy(
                phase = phase,
                feedback = feedback,
                isAutoCapture = isAutoCapture,
                progress = computeProgress(phase, detectionForTint),
                result = result,
            )
        }
    }

    suspend fun initAutoCapture() {
        val config = configRepository.getProjectConfiguration()
        isAutoCapture = isUsingAutoCaptureUseCase(config)
        if (isAutoCapture) {
            // Await until capture button is pressed
            holdOffAutoCapture()
        }
        emit() // Reset UI state with correct auto-capture value
    }

    fun initCapture(
        bioSdk: ModalitySdkType,
        samplesToCapture: Int,
        attemptNumber: Int,
    ) {
        Simber.i("Initialise face detection", tag = FACE_CAPTURE)
        this.samplesToCapture = samplesToCapture
        this.attemptNumber = attemptNumber
        viewModelScope.launch {
            faceDetector = resolveFaceBioSdk(bioSdk).detector

            val config = configRepository.getProjectConfiguration()
            spoofCheckConfig = getSpoofCheckConfiguration(config, bioSdk)
            qualityThreshold = config.face?.getSdkConfiguration(bioSdk)?.qualityThreshold ?: 0f
            autoCaptureImagingDurationMillis = config.experimental().faceAutoCaptureImagingDurationMillis
        }
    }

    fun holdOffAutoCapture() {
        if (isAutoCapture) {
            if (phase != LiveFeedbackState.Phase.NOT_STARTED) {
                return // too late - imaging has already started
            }
            isAutoCaptureHeldOff = true
            emit(phase = LiveFeedbackState.Phase.NOT_STARTED, feedback = LiveFeedbackState.Feedback.NONE) // reset view
        }
    }

    fun startCapture() {
        if (isAutoCapture) {
            isAutoCaptureHeldOff = false
        } else {
            emit(phase = LiveFeedbackState.Phase.CAPTURING)
        }
    }

    /**
     * Processes the image. Called on the CameraX analyzer executor (off the main thread).
     */
    fun process(
        originalBitmap: Bitmap,
        croppedBitmap: Bitmap,
    ) {
        // Skip processing and only update progress bar while spoof check is running
        if (phase == LiveFeedbackState.Phase.VALIDATING) {
            emit(phase = LiveFeedbackState.Phase.VALIDATING)
            originalBitmap.recycle()
            croppedBitmap.recycle()
            return
        } else if (phase == LiveFeedbackState.Phase.VALIDATION_FAILED) {
            originalBitmap.recycle()
            croppedBitmap.recycle()
            return
        }

        val captureStartTime = timeHelper.now()
        val potentialFace = faceDetector.analyze(croppedBitmap)

        val faceDetection = getFaceDetectionFromPotentialFace(originalBitmap, croppedBitmap, potentialFace)
        faceDetection.detectionStartTime = captureStartTime
        faceDetection.detectionEndTime = timeHelper.now()

        var newPhase = phase
        var feedback = _state.value.feedback

        if (isAutoCapture) {
            if (!isAutoCaptureHeldOff) {
                feedback = faceDetection.status.toFeedback()
                if (faceDetection.status == FaceDetection.Status.VALID && phase == LiveFeedbackState.Phase.NOT_STARTED) {
                    newPhase = LiveFeedbackState.Phase.CAPTURING
                    captureImagingStartTime = captureStartTime.ms
                    autoCaptureImagingTimeoutJob = viewModelScope.launch {
                        delay(autoCaptureImagingDurationMillis)
                        finishCapture(attemptNumber)
                    }
                }
            }
        } else {
            feedback = faceDetection.status.toFeedback()
        }

        when (newPhase) {
            LiveFeedbackState.Phase.NOT_STARTED -> updateFallbackCaptureIfValid(faceDetection)
            LiveFeedbackState.Phase.CAPTURING -> {
                if (isAutoCapture) {
                    if (isQualifying(faceDetection)) {
                        updateUserCapturesWith(faceDetection)
                    }
                } else {
                    userCaptures.add(faceDetection)
                    if (userCaptures.size == samplesToCapture) {
                        finishCapture(attemptNumber)
                    }
                }
            }

            else -> { // no-op
            }
        }

        emit(phase = newPhase, feedback = feedback, detectionForTint = faceDetection)
    }

    private fun computeProgress(
        phase: LiveFeedbackState.Phase,
        detection: FaceDetection?,
    ): Progress = Progress(
        value = normalizedProgress(phase),
        tint = when {
            phase == LiveFeedbackState.Phase.VALIDATING -> Progress.Tint.VALIDATION
            detection?.status == FaceDetection.Status.VALID_CAPTURING -> Progress.Tint.VALID
            else -> Progress.Tint.DEFAULT
        },
        visible = phase == LiveFeedbackState.Phase.CAPTURING || phase == LiveFeedbackState.Phase.VALIDATING,
    )

    private fun normalizedProgress(phase: LiveFeedbackState.Phase): Float = when {
        phase == LiveFeedbackState.Phase.VALIDATING ->
            ((timeHelper.now().ms - validationStartTime).toFloat() / spoofCheckConfig.validationUiDurationMs).coerceIn(0f, 1f)

        isAutoCapture ->
            ((timeHelper.now().ms - captureImagingStartTime).toFloat() / autoCaptureImagingDurationMillis).coerceIn(0f, 1f)

        else -> userCaptures.size.toFloat() / samplesToCapture
    }

    private fun isQualifying(faceDetection: FaceDetection): Boolean {
        if (autoCaptureImagingTimeoutJob?.isActive != true) {
            return false
        }
        if (!faceDetection.hasValidStatus()) {
            return false
        }
        val betterPreviousCaptureCount = userCaptures.count { previousCapture ->
            (previousCapture.face?.quality ?: -1f) > (faceDetection.face?.quality ?: -1f)
        }
        return betterPreviousCaptureCount < samplesToCapture
    }

    private fun updateUserCapturesWith(faceDetection: FaceDetection) {
        if (userCaptures.count() == samplesToCapture) {
            userCaptures.indices
                .minByOrNull { index ->
                    userCaptures[index].face?.quality ?: -1f
                }?.takeIf { it >= 0 }
                ?.let { worseQualityCaptureIndex ->
                    userCaptures[worseQualityCaptureIndex] = faceDetection
                }
        } else {
            userCaptures.add(faceDetection)
        }
    }

    /**
     * If any of the user captures are good, use them. If not, use the fallback capture.
     */
    private fun finishCapture(attemptNumber: Int) {
        Simber.i("Finish capture", tag = FACE_CAPTURE)
        viewModelScope.launch {
            if (spoofCheckConfig == SpoofCheckConfiguration.DISABLED) {
                sendEventsAndFinish(attemptNumber)
            } else {
                runSpoofChecksOnCaptures()

                if (spoofCheckConfig.mode == FaceConfiguration.SpoofCheckMode.RECORDED) {
                    sendEventsAndFinish(attemptNumber)
                } else {
                    val spoofCheckPassed = userCaptures.map { it.spoofCheckResult }.all {
                        Simber.i("Spoof check result for capture: $it", tag = FACE_CAPTURE)
                        it != null && it.skipReason == null && spoofCheckConfig.threshold > it.score
                    }

                    Simber.i("Spoof check passed: $spoofCheckPassed (check count: $spoofCheckCount)", tag = FACE_CAPTURE)
                    // Only attempt up to configured amount of times to prevent hard-blocking user
                    if (spoofCheckCount >= spoofCheckConfig.maxAttempts || spoofCheckPassed) {
                        sendEventsAndFinish(attemptNumber)
                    } else {
                        showValidationErrorAndReset()
                    }
                }
            }
        }
    }

    private suspend fun sendEventsAndFinish(attemptNumber: Int) {
        sortedQualifyingCaptures = userCaptures
            .filter { isAutoCapture || it.hasValidStatus() } // Auto-capture images are pre-qualified
            .sortedByDescending { it.face?.quality }
            .ifEmpty { listOfNotNull(fallbackCapture) }

        sendCaptureEvents(attemptNumber)
        emit(phase = LiveFeedbackState.Phase.FINISHED, result = sortedQualifyingCaptures)
    }

    private suspend fun runSpoofChecksOnCaptures() = withContext(bgDispatcher) {
        spoofCheckCount++
        validationStartTime = timeHelper.now().ms
        emit(phase = LiveFeedbackState.Phase.VALIDATING)

        val duration = measureTimedValue {
            for ((index, bitmap) in userCaptures.map { it.original }.withIndex()) {
                val result = faceDetector.spoofCheck(bitmap, spoofCheckConfig.maxBitmapSize)
                Simber.i("Spoof result: $result", tag = FACE_CAPTURE)
                userCaptures[index].spoofCheckResult = result
            }
        }

        // Show the UI for at least a moment for it to register with the user
        val delay = maxOf(spoofCheckConfig.validationUiDurationMs - duration.duration.inWholeMilliseconds, 0)
        Simber.i("Spoof check performed in ${duration.duration}, waiting for ${delay}ms", tag = FACE_CAPTURE)
        if (delay > 0) delay(delay.milliseconds)
    }

    private suspend fun showValidationErrorAndReset() {
        emit(phase = LiveFeedbackState.Phase.VALIDATION_FAILED)
        val duration = measureTimedValue {
            // Still track the capture attempt events for analytics and troubleshooting
            sendCaptureEvents(attemptNumber)

            userCaptures.forEach {
                it.original.recycle()
                it.bitmap.recycle()
            }
            userCaptures.clear()
            fallbackCapture?.original?.recycle()
            fallbackCapture?.bitmap?.recycle()
            fallbackCapture = null

            // Reset state
            isAutoCaptureHeldOff = true
            sortedQualifyingCaptures = emptyList()
        }
        val delay = maxOf(spoofCheckConfig.validationErrorUiDurationMs - duration.duration.inWholeMilliseconds, 0)
        Simber.i("Captures tracked in ${duration.duration}, waiting for ${delay}ms", tag = FACE_CAPTURE)
        if (delay > 0) delay(delay.milliseconds)

        emit(phase = LiveFeedbackState.Phase.NOT_STARTED, feedback = LiveFeedbackState.Feedback.NONE, result = emptyList())
    }

    private fun getFaceDetectionFromPotentialFace(
        original: Bitmap,
        bitmap: Bitmap,
        potentialFace: Face?,
    ): FaceDetection = if (potentialFace == null) {
        original.recycle()
        bitmap.recycle()
        FaceDetection(
            original = original,
            bitmap = bitmap,
            face = null,
            status = FaceDetection.Status.NOFACE,
            detectionStartTime = timeHelper.now(),
            detectionEndTime = timeHelper.now(),
        )
    } else {
        getFaceDetection(original, bitmap, potentialFace)
    }

    private fun getFaceDetection(
        original: Bitmap,
        bitmap: Bitmap,
        potentialFace: Face,
    ): FaceDetection {
        val areaOccupied = potentialFace.relativeBoundingBox.area()
        val status = when {
            areaOccupied < faceTarget.areaRange.start -> FaceDetection.Status.TOOFAR
            areaOccupied > faceTarget.areaRange.endInclusive -> FaceDetection.Status.TOOCLOSE
            potentialFace.yaw !in faceTarget.yawTarget -> FaceDetection.Status.OFFYAW
            potentialFace.roll !in faceTarget.rollTarget -> FaceDetection.Status.OFFROLL
            phase == LiveFeedbackState.Phase.CAPTURING -> FaceDetection.Status.VALID_CAPTURING
            else -> FaceDetection.Status.VALID
        }

        return FaceDetection(
            original = original,
            bitmap = bitmap,
            face = potentialFace,
            status = status,
            detectionStartTime = timeHelper.now(),
            detectionEndTime = timeHelper.now(),
        )
    }

    /**
     * While the user has not started the capture flow, we save fallback images. If the capture doesn't
     * get any good images, at least one good image will be saved
     */
    private fun updateFallbackCaptureIfValid(faceDetection: FaceDetection) {
        val fallbackQuality = fallbackCapture?.face?.quality ?: -1f // To ensure that detection is better with defaults
        val detectionQuality = faceDetection.face?.quality ?: 0f

        if (faceDetection.hasValidStatus() && detectionQuality >= fallbackQuality) {
            Simber.i("Fallback capture updated", tag = FACE_CAPTURE)
            fallbackCapture = faceDetection.apply { isFallback = true }
            createFirstFallbackCaptureEvent(faceDetection)
        }
    }

    /**
     * Send a fallback capture event only once
     */
    private fun createFirstFallbackCaptureEvent(faceDetection: FaceDetection) {
        if (shouldSendFallbackCaptureEvent.getAndSet(false)) {
            eventReporter.addFallbackCaptureEvent(
                fallbackCaptureEventStartTime,
                faceDetection.detectionEndTime,
            )
        }
    }

    /**
     * Since events are saved in a blocking way in [SimpleCaptureEventReporter.addCaptureEvents],
     * this method fans the writes out as parallel jobs on the background dispatcher so that
     * neither the main thread nor the camera pipeline is blocked.
     */
    private suspend fun sendCaptureEvents(attemptNumber: Int) = withContext(bgDispatcher) {
        userCaptures
            .map { async { sendCaptureEvent(it, attemptNumber) } }
            .plus(async { sendCaptureEvent(fallbackCapture, attemptNumber) })
            .awaitAll()
    }

    private suspend fun sendCaptureEvent(
        faceDetection: FaceDetection?,
        attemptNumber: Int,
    ) {
        if (faceDetection == null) return
        eventReporter.addCaptureEvents(faceDetection, attemptNumber, qualityThreshold, spoofCheckConfig, isAutoCapture = isAutoCapture)
    }

    companion object {
        private const val VALID_ROLL_DELTA = 15f
        private const val VALID_YAW_DELTA = 30f
    }
}
