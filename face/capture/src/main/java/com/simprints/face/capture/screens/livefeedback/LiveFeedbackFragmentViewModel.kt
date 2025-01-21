package com.simprints.face.capture.screens.livefeedback

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.tools.extentions.area
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.capture.models.FaceDetection
import com.simprints.face.capture.models.FaceTarget
import com.simprints.face.capture.models.SymmetricTarget
import com.simprints.face.capture.usecases.SimpleCaptureEventReporter
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.biosdkresolver.ResolveFaceBioSdkUseCase
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
internal class LiveFeedbackFragmentViewModel @Inject constructor(
    private val resolveFaceBioSdk: ResolveFaceBioSdkUseCase,
    private val configManager: ConfigManager,
    private val eventReporter: SimpleCaptureEventReporter,
    private val timeHelper: TimeHelper,
) : ViewModel() {
    private var attemptNumber: Int = 1
    private var samplesToCapture: Int = 1
    private var qualityThreshold: Float = 0f
    private var singleQualityFallbackCaptureRequired: Boolean = false

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
    private lateinit var faceDetector: FaceDetector

    /**
     * Processes the image
     *
     * @param croppedBitmap is the camera frame
     */
    fun process(croppedBitmap: Bitmap) {
        val captureStartTime = timeHelper.now()
        val potentialFace = faceDetector.analyze(croppedBitmap)

        val faceDetection = getFaceDetectionFromPotentialFace(croppedBitmap, potentialFace)
        faceDetection.detectionStartTime = captureStartTime
        faceDetection.detectionEndTime = timeHelper.now()

        currentDetection.postValue(faceDetection)

        when (capturingState.value) {
            CapturingState.NOT_STARTED -> updateFallbackCaptureIfValid(faceDetection)
            CapturingState.CAPTURING -> {
                userCaptures += faceDetection
                if (userCaptures.size == samplesToCapture) {
                    finishCapture(attemptNumber)
                }
            }

            else -> { // no-op
            }
        }
    }

    fun initCapture(
        samplesToCapture: Int,
        attemptNumber: Int,
    ) {
        Simber.tag(FACE_CAPTURE).i("Initialise face detection")

        this.samplesToCapture = samplesToCapture
        this.attemptNumber = attemptNumber
        viewModelScope.launch {
            faceDetector = resolveFaceBioSdk().detector

            val config = configManager.getProjectConfiguration()
            qualityThreshold = config.face?.qualityThreshold ?: 0f
            singleQualityFallbackCaptureRequired = config.experimental().singleQualityFallbackRequired
        }
    }

    fun startCapture() {
        capturingState.value = CapturingState.CAPTURING
    }

    /**
     * If any of the user captures are good, use them. If not, use the fallback capture.
     */
    private fun finishCapture(attemptNumber: Int) {
        Simber.tag(FACE_CAPTURE).i("Finish capture")
        viewModelScope.launch {
            sortedQualifyingCaptures = userCaptures
                .filter { it.hasValidStatus() }
                .sortedByDescending { it.face?.quality }
                .ifEmpty { listOfNotNull(fallbackCapture) }

            sendAllCaptureEvents(attemptNumber)

            capturingState.postValue(CapturingState.FINISHED)
        }
    }

    private fun getFaceDetectionFromPotentialFace(
        bitmap: Bitmap,
        potentialFace: Face?,
    ): FaceDetection = if (potentialFace == null) {
        bitmap.recycle()
        FaceDetection(
            bitmap = bitmap,
            face = null,
            status = FaceDetection.Status.NOFACE,
            detectionStartTime = timeHelper.now(),
            detectionEndTime = timeHelper.now(),
        )
    } else {
        getFaceDetection(bitmap, potentialFace)
    }

    private fun getFaceDetection(
        bitmap: Bitmap,
        potentialFace: Face,
    ): FaceDetection {
        val areaOccupied = potentialFace.relativeBoundingBox.area()
        val status = when {
            areaOccupied < faceTarget.areaRange.start -> FaceDetection.Status.TOOFAR
            areaOccupied > faceTarget.areaRange.endInclusive -> FaceDetection.Status.TOOCLOSE
            potentialFace.yaw !in faceTarget.yawTarget -> FaceDetection.Status.OFFYAW
            potentialFace.roll !in faceTarget.rollTarget -> FaceDetection.Status.OFFROLL
            shouldCheckQuality() && potentialFace.quality < qualityThreshold -> FaceDetection.Status.BAD_QUALITY
            capturingState.value == CapturingState.CAPTURING -> FaceDetection.Status.VALID_CAPTURING
            else -> FaceDetection.Status.VALID
        }

        return FaceDetection(
            bitmap = bitmap,
            face = potentialFace,
            status = status,
            detectionStartTime = timeHelper.now(),
            detectionEndTime = timeHelper.now(),
        )
    }

    private fun shouldCheckQuality() = !singleQualityFallbackCaptureRequired || fallbackCapture == null

    /**
     * While the user has not started the capture flow, we save fallback images. If the capture doesn't
     * get any good images, at least one good image will be saved
     */
    private fun updateFallbackCaptureIfValid(faceDetection: FaceDetection) {
        val fallbackQuality = fallbackCapture?.face?.quality ?: -1f // To ensure that detection is better with defaults
        val detectionQuality = faceDetection.face?.quality ?: 0f

        if (faceDetection.hasValidStatus() && detectionQuality >= fallbackQuality) {
            Simber.tag(FACE_CAPTURE).i("Fallback capture updated")
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
    private fun sendAllCaptureEvents(attemptNumber: Int) = runBlocking {
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
        eventReporter.addCaptureEvents(faceDetection, attemptNumber, qualityThreshold)
    }

    enum class CapturingState { NOT_STARTED, CAPTURING, FINISHED }

    companion object {
        private const val VALID_ROLL_DELTA = 15f
        private const val VALID_YAW_DELTA = 30f
    }
}
