package com.simprints.face.detection.mock

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.core.graphics.toRect
import com.simprints.face.capture.livefeedback.tools.CameraTargetOverlay
import com.simprints.face.detection.Face
import com.simprints.face.detection.FaceDetector
import com.simprints.face.models.FaceDetection
import com.simprints.uicomponents.models.PreviewFrame
import kotlin.random.Random

class MockFaceDetector : FaceDetector {
    private val mockDelay = 200L
    override fun analyze(previewFrame: PreviewFrame): Face? {
        return Face(
            previewFrame.width,
            previewFrame.height,
            CameraTargetOverlay.rectForPlane(
                previewFrame.width,
                previewFrame.height,
                previewFrame.width * 0.5f
            ).toRect(),
            0f,
            0f,
            0f,
            Random.Default.nextBytes(100),
            FaceDetection.TemplateFormat.MOCK
        )
    }

    override suspend fun analyze(bitmap: Bitmap): Face? {
        return Face(
            bitmap.width,
            bitmap.height,
            Rect(
                0, 0, bitmap.width, bitmap.height
            ),
            0f,
            0f,
            0f,
            Random.Default.nextBytes(100),
            FaceDetection.TemplateFormat.MOCK
        )
    }

}
