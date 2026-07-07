package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTimedValue

@HiltViewModel
internal class LiveFeedbackFragmentViewModel @Inject constructor(
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
    val currentDetection = MutableLiveData<FaceDetection>()
    val capturingState = MutableLiveData(CapturingState.NOT_STARTED)

    var isAutoCapture: Boolean = false
    var spoofCheckConfig: SpoofCheckConfiguration = SpoofCheckConfiguration.DISABLED
    var spoofCheckCount: Int = 0

    private var captureImagingStartTime: Long = 0
    private var isAutoCaptureHeldOff = true
    private var autoCaptureImagingTimeoutJob: Job? = null
    private var autoCaptureImagingDurationMillis: Long = FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT
    private lateinit var faceDetector: FaceDetector

    suspend fun initAutoCapture() {
        val config = configRepository.getProjectConfiguration()
        isAutoCapture = isUsingAutoCaptureUseCase(config)
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
            if (capturingState.value != CapturingState.NOT_STARTED) {
                return // too late - imaging has already started
            }
            capturingState.value = CapturingState.NOT_STARTED // reset view
            isAutoCaptureHeldOff = true
        }
    }

    fun startCapture() {
        if (isAutoCapture) {
            isAutoCaptureHeldOff = false
        } else {
            capturingState.value = CapturingState.CAPTURING
        }
    }

    /**
     * Processes the image
     *
     * @param croppedBitmap is the camera frame
     */
    fun process(
        originalBitmap: Bitmap,
        croppedBitmap: Bitmap,
    ) {
        if (capturingState.value == CapturingState.VALIDATING || capturingState.value == CapturingState.VALIDATION_FAILED) {
            // Skip processing while spoof check is running
            return
        }

        val captureStartTime = timeHelper.now()
        val potentialFace = faceDetector.analyze(croppedBitmap)

        val faceDetection = getFaceDetectionFromPotentialFace(originalBitmap, croppedBitmap, potentialFace)
        faceDetection.detectionStartTime = captureStartTime
        faceDetection.detectionEndTime = timeHelper.now()

        if (isAutoCapture) {
            if (!isAutoCaptureHeldOff) {
                currentDetection.postValue(faceDetection)
                if (faceDetection.status == FaceDetection.Status.VALID && capturingState.value == CapturingState.NOT_STARTED) {
                    capturingState.postValue(CapturingState.CAPTURING)
                    captureImagingStartTime = captureStartTime.ms
                    autoCaptureImagingTimeoutJob = viewModelScope.launch {
                        delay(autoCaptureImagingDurationMillis)
                        finishCapture(attemptNumber)
                    }
                }
            }
        } else {
            currentDetection.postValue(faceDetection)
        }

        when (capturingState.value) {
            CapturingState.NOT_STARTED -> updateFallbackCaptureIfValid(faceDetection)
            CapturingState.CAPTURING -> {
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
    }

    fun getNormalizedProgress(): Float = if (isAutoCapture) {
        ((timeHelper.now().ms - captureImagingStartTime).toFloat() / autoCaptureImagingDurationMillis).coerceIn(0f, 1f)
    } else {
        userCaptures.size.toFloat() / samplesToCapture
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

    private fun sendEventsAndFinish(attemptNumber: Int) {
        sortedQualifyingCaptures = userCaptures
            .filter { isAutoCapture || it.hasValidStatus() } // Auto-capture images are pre-qualified
            .sortedByDescending { it.face?.quality }
            .ifEmpty { listOfNotNull(fallbackCapture) }

        sendCaptureEvents(attemptNumber)
        capturingState.postValue(CapturingState.FINISHED)
    }

    private suspend fun runSpoofChecksOnCaptures() = withContext(bgDispatcher) {
        capturingState.postValue(CapturingState.VALIDATING)
        spoofCheckCount++

        val duration = measureTimedValue {
            for ((index, bitmap) in userCaptures.map { it.original }.withIndex()) {
                val result = faceDetector.spoofCheck(bitmap, spoofCheckConfig.maxBitmapSize)
                Simber.i("Spoof result: $result", tag = FACE_CAPTURE)
                userCaptures[index].spoofCheckResult = result
            }
        }

        // Show the UI for at least a moment for it to register with the user
        val delay = maxOf(MIN_SPOOF_CHECK_UI_TIME_MS - duration.duration.inWholeMilliseconds, 0)
        Simber.i("Spoof check performed in ${duration.duration}, waiting for ${delay}ms", tag = FACE_CAPTURE)
        if (delay > 0) delay(delay.milliseconds)
    }

    private suspend fun showValidationErrorAndReset() {
        capturingState.postValue(CapturingState.VALIDATION_FAILED)
        val duration = measureTimedValue {
            // Still track the capture attempt events for analytics and troubleshooting
            sendCaptureEvents(attemptNumber)
        }
        val delay = maxOf(MIN_SPOOF_CHECK_UI_TIME_MS - duration.duration.inWholeMilliseconds, 0)
        Simber.i("Captures tracked in ${duration.duration}, waiting for ${delay}ms", tag = FACE_CAPTURE)
        if (delay > 0) delay(delay.milliseconds)

        // Reset state
        userCaptures.clear()
        sortedQualifyingCaptures = emptyList()
        fallbackCapture = null
        capturingState.postValue(CapturingState.NOT_STARTED)
    }

    private fun getFaceDetectionFromPotentialFace(
        original: Bitmap,
        bitmap: Bitmap,
        potentialFace: Face?,
    ): FaceDetection = if (potentialFace == null) {
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
            capturingState.value == CapturingState.CAPTURING -> FaceDetection.Status.VALID_CAPTURING
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
     * to speed things up this method creates multiple async jobs and run them all in parallel.
     */
    private fun sendCaptureEvents(attemptNumber: Int) = runBlocking {
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
        eventReporter.addCaptureEvents(faceDetection, attemptNumber, qualityThreshold, isAutoCapture = isAutoCapture)
    }

    enum class CapturingState { NOT_STARTED, CAPTURING, VALIDATING, VALIDATION_FAILED, FINISHED }

    companion object {
        private const val VALID_ROLL_DELTA = 15f
        private const val VALID_YAW_DELTA = 30f

        private const val MIN_SPOOF_CHECK_UI_TIME_MS = 2000
    }
}
