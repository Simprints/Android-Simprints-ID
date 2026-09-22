package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherBG
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.tools.extensions.area
import com.simprints.core.tools.extensions.normalisedIn
import com.simprints.core.tools.extensions.scaledTo
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.models.FaceTarget
import com.simprints.face.capture.models.FaceTrackingConfiguration
import com.simprints.face.capture.models.SymmetricTarget
import com.simprints.face.capture.screens.CaptureAttemptTracker
import com.simprints.face.capture.usecases.CropToFaceSquareUseCase
import com.simprints.face.capture.usecases.GetFaceTrackingConfigurationUseCase
import com.simprints.face.capture.usecases.GetSpoofCheckConfigurationUseCase
import com.simprints.face.capture.usecases.IsUsingAutoCaptureUseCase
import com.simprints.face.capture.usecases.SelectDominantFaceUseCase
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.detection.FaceSelector
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT
import com.simprints.infra.config.store.models.FaceConfiguration.SpoofCheckConfiguration
import com.simprints.infra.config.store.models.FaceConfiguration.SpoofCheckMode
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTimedValue

@HiltViewModel
internal class LiveFeedbackViewModel @Inject constructor(
    private val resolveFaceBioSdk: ResolveFaceBioSdkUseCase,
    private val configRepository: ConfigRepository,
    private val eventReporter: SimpleCaptureEventReporter,
    private val timeHelper: TimeHelper,
    private val isUsingAutoCaptureUseCase: IsUsingAutoCaptureUseCase,
    private val getFaceTrackingConfiguration: GetFaceTrackingConfigurationUseCase,
    private val getSpoofCheckConfiguration: GetSpoofCheckConfigurationUseCase,
    private val captureAttemptTracker: CaptureAttemptTracker,
    private val cropToFaceSquare: CropToFaceSquareUseCase,
    private val selectDominantFace: SelectDominantFaceUseCase,
    @param:DispatcherBG private val bgDispatcher: CoroutineDispatcher,
) : ViewModel() {
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

    val state: StateFlow<LiveFeedbackState>
        field = MutableStateFlow(LiveFeedbackState.initial())
    val permissionActions: SharedFlow<PermissionAction>
        field = MutableSharedFlow<PermissionAction>(extraBufferCapacity = 1)

    var isAutoCapture: Boolean = false
    private var spoofCheckConfig: SpoofCheckConfiguration = SpoofCheckConfiguration.DISABLED
    private var spoofCheckCount: Int = 0

    private var captureImagingStartTime: Long = 0
    private var validationStartTime: Long = 0
    var isAutoCaptureHeldOff = true
        private set

    private var faceTracking = FaceTrackingConfiguration.DISABLED

    private var autoCaptureImagingTimeoutJob: Job? = null
    private var autoCaptureImagingDurationMillis: Long = FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT
    private lateinit var faceDetector: FaceDetector
    private var hasAutoRequestedPermission = false

    private val phase: LiveFeedbackState.Phase
        get() = state.value.phase

    private fun emit(
        phase: LiveFeedbackState.Phase = state.value.phase,
        feedback: LiveFeedbackState.Feedback = state.value.feedback,
        permissionStatus: PermissionStatus = state.value.permissionStatus,
        detectionForTint: FaceDetection? = null,
        targetBox: FaceTargetBox? = state.value.targetBox,
        result: List<FaceDetection> = state.value.result,
        stateInitialised: Boolean? = null,
    ) {
        state.update { currentState ->
            currentState.copy(
                stateInitialised = stateInitialised ?: currentState.stateInitialised,
                phase = phase,
                feedback = feedback,
                isAutoCapture = isAutoCapture,
                isFaceTrackingEnabled = faceTracking.enabled,
                isProgressAroundCaptureButton = faceTracking.progressAroundCaptureButton,
                permissionStatus = permissionStatus,
                progress = computeProgress(phase, detectionForTint),
                targetBox = targetBox,
                result = result,
            )
        }
    }

    suspend fun initAutoCapture() {
        val config = configRepository.getProjectConfiguration()
        faceTracking = getFaceTrackingConfiguration(config)
        isAutoCapture = isUsingAutoCaptureUseCase(config)
        if (isAutoCapture) {
            // Await until capture button is pressed
            holdOffAutoCapture()
        }
        emit(stateInitialised = true) // Reset UI state with correct auto-capture value
    }

    fun onScreenResumed(permissionStatus: PermissionStatus) {
        emit(permissionStatus = permissionStatus)
        if (permissionStatus == PermissionStatus.Granted) {
            hasAutoRequestedPermission = false
            return
        }
        if (permissionStatus == PermissionStatus.Denied && !hasAutoRequestedPermission) {
            hasAutoRequestedPermission = true
            permissionActions.tryEmit(PermissionAction.RequestCameraPermission)
        }
    }

    fun onPermissionResult(permissionStatus: PermissionStatus) {
        emit(permissionStatus = permissionStatus)
        if (permissionStatus == PermissionStatus.Granted) {
            hasAutoRequestedPermission = false
        }
    }

    fun onPermissionButtonClicked() {
        when (state.value.permissionStatus) {
            PermissionStatus.DeniedNeverAskAgain -> permissionActions.tryEmit(PermissionAction.OpenAppSettings)
            PermissionStatus.Granted -> Unit
            PermissionStatus.Denied -> permissionActions.tryEmit(PermissionAction.RequestCameraPermission)
        }
    }

    fun initCapture(
        bioSdk: ModalitySdkType,
        samplesToCapture: Int,
    ) {
        Simber.i("Initialise face detection", tag = FACE_CAPTURE)
        this.samplesToCapture = samplesToCapture
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
            // reset view
            emit(
                phase = LiveFeedbackState.Phase.NOT_STARTED,
                feedback = LiveFeedbackState.Feedback.NONE,
                targetBox = null,
            )
        }
    }

    fun startCapture() {
        // Single path for every new capture attempt
        captureAttemptTracker.onNewCaptureAttemptStarted()
        if (isAutoCapture) {
            isAutoCaptureHeldOff = false
        } else {
            emit(phase = LiveFeedbackState.Phase.CAPTURING)
        }
    }

    /**
     * Evaluates existing state to determine if the capture session is logically finished
     */
    private fun isCaptureComplete(): Boolean {
        if (phase != LiveFeedbackState.Phase.CAPTURING) return false

        return if (isAutoCapture) {
            // Auto mode finishes when the imaging duration expires
            (timeHelper.now().ms - captureImagingStartTime) >= autoCaptureImagingDurationMillis
        } else {
            // Manual mode finishes when the requested sample count is met
            userCaptures.size >= samplesToCapture
        }
    }

    /**
     * Processes the image. Called on the CameraX analyzer executor (off the main thread).
     *
     * [frame] covers the whole visible preview: the face is detected anywhere within it, and the
     * square around the detection is what gets cropped for the rest of the pipeline.
     */
    fun process(
        originalBitmap: Bitmap,
        croppedBitmap: Bitmap,
    ) {
        // Drop frames if capture is complete to avoid race conditions.
        if (isCaptureComplete()) {
            originalBitmap.recycle()
            croppedBitmap.recycle()
            return
        }
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
        val potentialFace = if (faceTracking.enabled) {
            // Detection covers the whole preview, so the subject is picked out of whatever is in
            // frame and only that face has a template extracted for it
            faceDetector.analyze(croppedBitmap) { faces ->
                selectDominantFace(faces, croppedBitmap.width, croppedBitmap.height)
            }
        } else {
            faceDetector.analyze(croppedBitmap)
        }
        val trackedSquare = trackedSquareFor(potentialFace, croppedBitmap)
        val frameWidth = croppedBitmap.width
        val frameHeight = croppedBitmap.height

        val faceDetection = getFaceDetectionFromPotentialFace(originalBitmap, croppedBitmap, potentialFace)
        faceDetection.detectionStartTime = captureStartTime
        faceDetection.detectionEndTime = timeHelper.now()

        var newPhase = phase
        var feedback = state.value.feedback

        if (isAutoCapture) {
            if (!isAutoCaptureHeldOff) {
                feedback = faceDetection.status.toFeedback()
                if (faceDetection.status == FaceDetection.Status.VALID && phase == LiveFeedbackState.Phase.NOT_STARTED) {
                    newPhase = LiveFeedbackState.Phase.CAPTURING
                    captureImagingStartTime = captureStartTime.ms
                    autoCaptureImagingTimeoutJob = viewModelScope.launch {
                        delay(autoCaptureImagingDurationMillis)
                        finishCapture(captureAttemptTracker.attemptNumber)
                    }
                }
            }
        } else {
            feedback = if (fallbackCapture != null && faceDetection.status == FaceDetection.Status.BAD_QUALITY) {
                // In some environment it might be difficult to maintain the image quality for long enough to press capture button,
                // therefore once we have at least one good quality fallback capture, the user can ignore the "bad_quality" state
                // and proceed with capture as long as the face is in the correct position.
                LiveFeedbackState.Feedback.VALID
            } else {
                faceDetection.status.toFeedback()
            }
        }

        when (newPhase) {
            LiveFeedbackState.Phase.NOT_STARTED -> updateFallbackCaptureIfValid(faceDetection, trackedSquare)
            LiveFeedbackState.Phase.CAPTURING -> {
                if (isAutoCapture) {
                    if (isQualifying(faceDetection)) {
                        updateUserCapturesWith(cropBitmapToTrackedSquare(faceDetection, trackedSquare))
                    } else {
                        releaseFrames(faceDetection)
                    }
                } else {
                    userCaptures.add(cropBitmapToTrackedSquare(faceDetection, trackedSquare))
                    if (userCaptures.size == samplesToCapture) {
                        finishCapture(captureAttemptTracker.attemptNumber)
                    }
                }
            }

            else -> { // no-op
            }
        }

        emit(
            phase = newPhase,
            feedback = feedback,
            detectionForTint = faceDetection,
            targetBox = trackedSquare?.let {
                FaceTargetBox(it.normalisedIn(frameWidth, frameHeight), faceDetection.status.toTargetTint())
            },
        )
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
                    releaseFrames(userCaptures[worseQualityCaptureIndex])
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
            enrichCapturesWithAgeAndGender()
            if (spoofCheckConfig.mode == SpoofCheckMode.DISABLED) {
                sendEventsAndFinish(attemptNumber)
            } else {
                runSpoofChecksOnCaptures()

                if (spoofCheckConfig.mode == SpoofCheckMode.RECORDED) {
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
                // Only face tracking allows more than one person in frame, so only it needs a
                // policy for picking between them - cutout capture stays on the SDK's own choice
                val selectFace: FaceSelector? = if (faceTracking.enabled) {
                    { faces -> selectDominantFace(faces, bitmap.width, bitmap.height) }
                } else {
                    null
                }
                val result = faceDetector.spoofCheck(bitmap, spoofCheckConfig.maxBitmapSize, selectFace)
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
            sendCaptureEvents(captureAttemptTracker.attemptNumber)

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

        emit(
            phase = LiveFeedbackState.Phase.NOT_STARTED,
            feedback = LiveFeedbackState.Feedback.NONE,
            targetBox = null,
            result = emptyList(),
        )
    }

    /**
     * Face tracking only: the square to crop and draw around the subject, in frame pixels. Null
     * for cutout capture, where the frame handed in is already cropped to the on-screen target.
     */
    private fun trackedSquareFor(
        potentialFace: Face?,
        frame: Bitmap,
    ): Rect? {
        if (!faceTracking.enabled || potentialFace == null) return null
        val faceBox = potentialFace.relativeBoundingBox.scaledTo(frame.width, frame.height)
        return cropToFaceSquare.squareFor(faceBox, frame.width, frame.height).takeUnless { it.isEmpty }
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
    ): FaceDetection = FaceDetection(
        original = original,
        bitmap = bitmap,
        face = potentialFace,
        status = if (faceTracking.enabled) trackedFaceStatus(potentialFace, bitmap) else cutoutFaceStatus(potentialFace),
        detectionStartTime = timeHelper.now(),
        detectionEndTime = timeHelper.now(),
    )

    /**
     * Cropping costs an allocation and a rescale on the analyzer thread, so it is
     * done here rather than for every analysed frame - the overwhelming majority are discarded
     * straight after their status is read.
     */
    private fun cropBitmapToTrackedSquare(
        faceDetection: FaceDetection,
        trackedSquare: Rect?,
    ): FaceDetection {
        if (trackedSquare == null) return faceDetection
        val crop = cropToFaceSquare(faceDetection.bitmap, trackedSquare, faceTracking.maxImageSizePx)
        // The use case hands the frame straight back when the square is unusable
        if (crop === faceDetection.bitmap) return faceDetection
        // Only a copy made for this screen is ours to release.
        if (faceDetection.bitmap !== faceDetection.original) faceDetection.bitmap.recycle()
        return faceDetection.copy(bitmap = crop)
    }

    /** Cutout capture judges distance by how much of the fixed on-screen target the face fills. */
    private fun cutoutFaceStatus(potentialFace: Face): FaceDetection.Status {
        val areaOccupied = potentialFace.relativeBoundingBox.area()
        return when {
            areaOccupied < faceTarget.areaRange.start -> FaceDetection.Status.TOOFAR
            areaOccupied > faceTarget.areaRange.endInclusive -> FaceDetection.Status.TOOCLOSE
            else -> poseAndQualityStatus(potentialFace)
        }
    }

    /**
     * Face tracking judges distance by the face's own pixel size, since it can sit anywhere in the
     * preview. Those are the pixels the SDK actually saw, which is what governs whether a usable
     * template can be extracted from it.
     */
    private fun trackedFaceStatus(
        potentialFace: Face,
        frame: Bitmap,
    ): FaceDetection.Status {
        val faceBox = potentialFace.relativeBoundingBox.scaledTo(frame.width, frame.height)
        val detectedSide = max(faceBox.width(), faceBox.height())
        val frameSide = min(frame.width, frame.height)
        // The square is the detection's longer side, so this is the size the stored crop would have
        val smallestUsableSide = max(faceTracking.minFaceSizePx.toFloat(), frameSide * MIN_FACE_FRAME_RATIO)
        return when {
            detectedSide < smallestUsableSide -> FaceDetection.Status.TOOFAR
            detectedSide > frameSide * MAX_FACE_FRAME_RATIO -> FaceDetection.Status.TOOCLOSE
            else -> poseAndQualityStatus(potentialFace)
        }
    }

    /** Everything the two capture modes judge the same way, once distance is settled. */
    private fun poseAndQualityStatus(potentialFace: Face): FaceDetection.Status = when {
        potentialFace.yaw !in faceTarget.yawTarget -> FaceDetection.Status.OFFYAW
        potentialFace.roll !in faceTarget.rollTarget -> FaceDetection.Status.OFFROLL
        potentialFace.quality < qualityThreshold -> FaceDetection.Status.BAD_QUALITY
        phase == LiveFeedbackState.Phase.CAPTURING -> FaceDetection.Status.VALID_CAPTURING
        else -> FaceDetection.Status.VALID
    }

    /**
     * While the user has not started the capture flow, we save fallback images. If the capture doesn't
     * get any good images, at least one good image will be saved
     */
    private fun updateFallbackCaptureIfValid(
        faceDetection: FaceDetection,
        trackedSquare: Rect?,
    ) {
        val fallbackQuality = fallbackCapture?.face?.quality ?: -1f // To ensure that detection is better with defaults
        val detectionQuality = faceDetection.face?.quality ?: 0f

        if (faceDetection.hasValidStatus() && detectionQuality >= fallbackQuality) {
            Simber.i("Fallback capture updated", tag = FACE_CAPTURE)
            val kept = cropBitmapToTrackedSquare(faceDetection, trackedSquare).apply { isFallback = true }
            fallbackCapture?.let { releaseFrames(it) }
            fallbackCapture = kept
            createFirstFallbackCaptureEvent(kept)
        } else {
            releaseFrames(faceDetection)
        }
    }

    private fun releaseFrames(faceDetection: FaceDetection) {
        faceDetection.original.recycle()
        faceDetection.bitmap.recycle()
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

    private suspend fun enrichCapturesWithAgeAndGender() = withContext(bgDispatcher) {
        userCaptures.forEachIndexed { index, faceDetection ->
            userCaptures[index] = enrichWithAgeAndGender(faceDetection) ?: faceDetection
        }
        fallbackCapture = enrichWithAgeAndGender(fallbackCapture)
    }

    private fun enrichWithAgeAndGender(faceDetection: FaceDetection?): FaceDetection? {
        val face = faceDetection?.face ?: return faceDetection
        val ageAndGender = faceDetector.analyze(faceDetection.bitmap, estimateAgeAndGender = true) ?: return faceDetection
        return faceDetection.copy(face = face.copy(age = ageAndGender.age, gender = ageAndGender.gender))
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

        /**
         * How big the face has to be for face tracking, as a fraction of the preview's shorter
         * edge. The lower bound asks for about as much face as the cutout does; the upper one is
         * deliberately loose, since a square that follows the face only stops working once it no
         * longer fits the preview.
         *
         * Expressed as proportions rather than pixels because the analyser resolution follows the
         * preview size - a fixed pixel band would mean a different thing on every device.
         */
        private const val MIN_FACE_FRAME_RATIO = 0.4f
        private const val MAX_FACE_FRAME_RATIO = 0.9f
    }

    enum class PermissionAction {
        RequestCameraPermission,
        OpenAppSettings,
    }
}
