package com.simprints.face.capture.livefeedback

import android.graphics.RectF
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.otaliastudios.cameraview.frame.Frame
import com.simprints.core.tools.extentions.area
import com.simprints.core.tools.utils.randomUUID
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.capture.livefeedback.tools.FrameProcessor
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.FaceCaptureEvent
import com.simprints.face.controllers.core.events.model.FaceFallbackCaptureEvent
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.detection.Face
import com.simprints.face.detection.FaceDetector
import com.simprints.face.models.FaceDetection
import com.simprints.face.models.FaceTarget
import com.simprints.face.models.FloatRange
import com.simprints.face.models.PreviewFrame
import com.simprints.face.models.Size
import com.simprints.face.models.SymmetricTarget
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean

class LiveFeedbackFragmentViewModel(
    private val mainVM: FaceCaptureViewModel,
    private val faceDetector: FaceDetector,
    private val frameProcessor: FrameProcessor,
    private val qualityThreshold: Float,
    private val faceSessionEventsManager: FaceSessionEventsManager,
    private val faceTimeHelper: FaceTimeHelper
) : ViewModel() {
    private val faceTarget = FaceTarget(
        SymmetricTarget(VALID_YAW_DELTA),
        SymmetricTarget(VALID_ROLL_DELTA),
        FloatRange(0.25f, 0.5f)
    )
    private val fallbackCaptureEventStartTime = faceTimeHelper.now()
    private var shouldSendFallbackCaptureEvent: AtomicBoolean = AtomicBoolean(true)

    lateinit var fallbackCapture: FaceDetection
    val userCaptures = mutableListOf<FaceDetection>()
    val currentDetection = MutableLiveData<FaceDetection>()
    val capturingState = MutableLiveData(CapturingState.NOT_STARTED)

    /**
     * Processes the image
     *
     * @param frame is the camera frame
     * @param faceRectF is the box on the screen
     * @param size is the screen size
     */
    fun process(frame: Frame, faceRectF: RectF, size: Size) {
        val captureStartTime = faceTimeHelper.now()
        val previewFrame = frameProcessor.previewFrameFrom(frame, faceRectF, size, false)

        val potentialFace = faceDetector.analyze(previewFrame)

        val faceDetection = getFaceDetectionFromPotentialFace(potentialFace, previewFrame)
        faceDetection.detectionStartTime = captureStartTime
        faceDetection.detectionEndTime = faceTimeHelper.now()

        currentDetection.postValue(faceDetection)

        when (capturingState.value) {
            CapturingState.NOT_STARTED -> updateFallbackCaptureIfValid(faceDetection)
            CapturingState.CAPTURING -> {
                userCaptures += faceDetection
                if (userCaptures.size == mainVM.samplesToCapture) {
                    finishCapture()
                }
            }
            else -> {//no-op
            }
        }
    }

    fun startCapture() {
        capturingState.value = CapturingState.CAPTURING
    }

    /**
     * If any of the user captures are good, use them. If not, use the fallback capture.
     */
    private fun finishCapture() {
        val sortedQualifyingCaptures = userCaptures
            .filter { it.hasValidStatus() && it.isAboveQualityThreshold(qualityThreshold) }
            .sortedByDescending { it.face?.quality }
            .ifEmpty { listOf(fallbackCapture) }

        sendAllCaptureEvents()

        capturingState.postValue(CapturingState.FINISHED)
        mainVM.captureFinished(sortedQualifyingCaptures)
    }

    private fun getFaceDetectionFromPotentialFace(
        potentialFace: Face?,
        previewFrame: PreviewFrame
    ): FaceDetection {
        return if (potentialFace == null) {
            FaceDetection(previewFrame, potentialFace, FaceDetection.Status.NOFACE)
        } else {
            getFaceDetection(potentialFace, previewFrame)
        }
    }

    private fun getFaceDetection(potentialFace: Face, previewFrame: PreviewFrame): FaceDetection {
        val areaOccupied = potentialFace.relativeBoundingBox.area()
        return when {
            areaOccupied < faceTarget.areaRange.start -> FaceDetection(
                previewFrame,
                potentialFace,
                FaceDetection.Status.TOOFAR
            )
            areaOccupied > faceTarget.areaRange.endInclusive -> FaceDetection(
                previewFrame,
                potentialFace,
                FaceDetection.Status.TOOCLOSE
            )
            potentialFace.yaw !in faceTarget.yawTarget -> FaceDetection(
                previewFrame,
                potentialFace,
                FaceDetection.Status.OFFYAW
            )
            potentialFace.roll !in faceTarget.rollTarget -> FaceDetection(
                previewFrame,
                potentialFace,
                FaceDetection.Status.OFFROLL
            )
            else -> FaceDetection(
                previewFrame,
                potentialFace,
                if (capturingState.value == CapturingState.CAPTURING) FaceDetection.Status.VALID_CAPTURING else FaceDetection.Status.VALID
            )
        }
    }

    /**
     * While the user has not started the capture flow, we save fallback images. If the capture doesn't
     * get any good images, at least one good image will be saved
     */
    private fun updateFallbackCaptureIfValid(faceDetection: FaceDetection) {
        if (faceDetection.hasValidStatus()) {
            fallbackCapture = faceDetection.apply { isFallback = true }
            createFirstFallbackCaptureEvent(faceDetection)
        }
    }

    /**
     * Send a fallback capture event only once
     */
    private fun createFirstFallbackCaptureEvent(faceDetection: FaceDetection) {
        if (shouldSendFallbackCaptureEvent.getAndSet(false)) {
            faceSessionEventsManager.addEventInBackground(
                FaceFallbackCaptureEvent(
                    fallbackCaptureEventStartTime,
                    faceDetection.detectionEndTime
                )
            )
        }
    }

    private fun sendCaptureEvent(faceDetection: FaceDetection) {
        val payloadId = randomUUID() // The payloads of these two events need to have the same ids
        val faceCaptureEvent =
            faceDetection.toFaceCaptureEvent(mainVM.attemptNumber, qualityThreshold, payloadId)

        val faceCaptureBiometricsEvent =
            if (faceCaptureEvent.result == FaceCaptureEvent.Result.VALID) faceDetection.toFaceCaptureBiometricsEvent(
                payloadId
            ) else null

        faceSessionEventsManager.addEvent(faceCaptureEvent)
        faceCaptureBiometricsEvent?.let { faceSessionEventsManager.addEvent(it) }

        faceDetection.id = faceCaptureEvent.id
    }

    /**
     * Since all events are saved in a blocking way
     * [FaceSessionEventsManagerImpl.addEvent][com.simprints.face.controllers.core.events.FaceSessionEventsManagerImpl.addEvent],
     * to speed things up this method creates multiple async jobs and run them all in parallel.
     *
     * This is already running in a background Thread because of CameraView, don't worry about the runBlocking.
     */
    private fun sendAllCaptureEvents() = runBlocking {
        val allDeferredEvents = mutableListOf<Deferred<Unit>>()
        allDeferredEvents += userCaptures.map { async { sendCaptureEvent(it) } }
        allDeferredEvents += async { sendCaptureEvent(fallbackCapture) }
        allDeferredEvents.awaitAll()
    }

    enum class CapturingState { NOT_STARTED, CAPTURING, FINISHED }

    companion object {
        private const val VALID_ROLL_DELTA = 15f
        private const val VALID_YAW_DELTA = 30f
    }
}
