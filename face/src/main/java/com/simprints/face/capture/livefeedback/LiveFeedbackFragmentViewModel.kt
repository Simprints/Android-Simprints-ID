package com.simprints.face.capture.livefeedback

import android.graphics.RectF
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.otaliastudios.cameraview.frame.Frame
import com.simprints.core.tools.extentions.area
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.capture.livefeedback.tools.FrameProcessor
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.FaceFallbackCaptureEvent
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.detection.Face
import com.simprints.face.detection.FaceDetector
import com.simprints.face.models.FaceDetection
import com.simprints.face.models.FaceTarget
import com.simprints.face.models.SymmetricTarget
import com.simprints.uicomponents.models.FloatRange
import com.simprints.uicomponents.models.PreviewFrame
import com.simprints.uicomponents.models.Size
import kotlinx.coroutines.channels.Channel

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
    private var fallbackCaptureEventSent = false

    lateinit var fallbackCapture: FaceDetection
    val userCaptures = mutableListOf<FaceDetection>()
    val currentDetection = MutableLiveData<FaceDetection>()
    val capturingState = MutableLiveData(CapturingState.NOT_STARTED)

    val frameChannel = Channel<Frame>(Channel.CONFLATED)

    suspend fun process(
        frame: Frame,
        faceRectF: RectF,
        size: Size
    ) {
        val captureStartTime = faceTimeHelper.now()
        val previewFrame = frameProcessor.previewFrameFrom(frame, faceRectF, size, false)

        val potentialFace = faceDetector.analyze(previewFrame)

        val faceDetection = getFaceDetectionFromPotentialFace(potentialFace, previewFrame)
        faceDetection.detectionStartTime = captureStartTime
        faceDetection.detectionEndTime = faceTimeHelper.now()

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

        currentDetection.value = faceDetection
    }

    fun startCapture() {
        capturingState.value = CapturingState.CAPTURING
    }

    fun handlePreviewFrame(frame: Frame) {
        frameChannel.offer(frame)
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

        capturingState.value = CapturingState.FINISHED
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
        if (fallbackCaptureEventSent) return

        fallbackCaptureEventSent = true
        faceSessionEventsManager.addEventInBackground(
            FaceFallbackCaptureEvent(
                fallbackCaptureEventStartTime,
                faceDetection.detectionEndTime
            )
        )
    }

    private fun sendCaptureEvent(faceDetection: FaceDetection) {
        val faceCaptureEvent =
            faceDetection.toFaceCaptureEvent(mainVM.attemptNumber, qualityThreshold)

        faceSessionEventsManager.addEventInBackground(faceCaptureEvent)

        faceDetection.id = faceCaptureEvent.id
    }

    private fun sendAllCaptureEvents() {
        userCaptures.forEach { sendCaptureEvent(it) }
        sendCaptureEvent(fallbackCapture)
    }

    enum class CapturingState { NOT_STARTED, CAPTURING, FINISHED, FINISHED_FAILED }

    companion object {
        private const val VALID_ROLL_DELTA = 15f
        private const val VALID_YAW_DELTA = 30f
    }
}
