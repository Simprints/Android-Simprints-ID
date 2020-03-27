package com.simprints.face.capture.livefeedback

import android.graphics.RectF
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.otaliastudios.cameraview.frame.Frame
import com.simprints.core.data.analytics.AnalyticsManager
import com.simprints.core.data.preferences.CameraPreferences
import com.simprints.core.extensions.set
import com.simprints.face.capture.FaceCaptureViewModel
import com.simprints.face.capture.livefeedback.tools.FrameProcessor
import com.simprints.face.detection.FaceDetector
import com.simprints.face.models.FaceDetection
import com.simprints.face.models.FaceTarget
import com.simprints.face.models.SymmetricTarget
import com.simprints.uicomponents.extensions.area
import com.simprints.uicomponents.models.FloatRange
import com.simprints.uicomponents.models.Size

class LiveFeedbackFragmentViewModel(
    private val mainVM: FaceCaptureViewModel,
    private val faceDetector: FaceDetector,
    private val cameraPreferences: CameraPreferences,
    private val analyticsManager: AnalyticsManager,
    private val frameProcessor: FrameProcessor
) : ViewModel() {
    private val faceTarget = FaceTarget(
        SymmetricTarget(VALID_YAW_DELTA),
        SymmetricTarget(VALID_ROLL_DELTA),
        FloatRange(0.25f, 0.5f)
    )

    val currentDetection = MutableLiveData<FaceDetection>()
    val capturing = MutableLiveData<CapturingState>(CapturingState.NOT_STARTED)

    val captures = mutableListOf<FaceDetection>()

    private val qualityThreshold = cameraPreferences.qualityThreshold

    suspend fun process(
        frame: Frame,
        faceRectF: RectF,
        size: Size
    ) {
        val previewFrame = frameProcessor.previewFrameFrom(
            frame,
            faceRectF,
            size,
            cameraPreferences.getFacingFront()
        )

        val potentialFace = faceDetector.analyze(previewFrame)

        val faceDetection: FaceDetection = if (potentialFace == null) {
            FaceDetection(previewFrame, potentialFace, FaceDetection.Status.NOFACE)
        } else {
            val areaOccupied = potentialFace.relativeBoundingBox.area()
            when {
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
                    if (capturing.value == CapturingState.CAPTURING) FaceDetection.Status.VALID_CAPTURING else FaceDetection.Status.VALID
                )
            }
        }

        if (capturing.value == CapturingState.CAPTURING) {
            captures += faceDetection
            if (captures.size == mainVM.samplesToCapture) {
                captureFinished()
            }
        }

        currentDetection.set(faceDetection)
    }

    private fun checkCurrentFaceLag() = currentDetection.value?.detectionTime?.let {
        if ((System.currentTimeMillis() - it) > READY_STATE_LAG_MS) currentDetection.set(null)
    }

    fun startCapture() {
        capturing.set(CapturingState.CAPTURING)
    }

    private fun captureFinished() {
        val bestCaptures = captures
            .sortedByDescending { it.face?.quality }
            .also { mainVM.captureFinished(it) }
            .filter { it.status == FaceDetection.Status.VALID_CAPTURING }

        if (bestCaptures.isEmpty() || bestCaptures.first().face?.quality ?: Float.NEGATIVE_INFINITY < qualityThreshold) {
            capturing.set(CapturingState.FINISHED_FAILED)
        } else {
            capturing.set(CapturingState.FINISHED)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mainVM.stopFaceDetection()
    }

    enum class CapturingState { NOT_STARTED, CAPTURING, FINISHED, FINISHED_FAILED }

    companion object {
        private const val VALID_ROLL_DELTA = 15f
        private const val VALID_YAW_DELTA = 30f

        private const val READY_STATE_LAG_MS = 500
    }
}
