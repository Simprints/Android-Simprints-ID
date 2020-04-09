package com.simprints.face.detection.mock

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.core.graphics.toRect
import com.simprints.face.capture.livefeedback.tools.CameraTargetOverlay
import com.simprints.face.detection.Face
import com.simprints.face.detection.FaceDetector
import com.simprints.uicomponents.models.PreviewFrame
import kotlinx.coroutines.delay

class MockFaceDetector : FaceDetector {
    private val mockDelay = 200L
    override suspend fun analyze(previewFrame: PreviewFrame): Face? {
        delay(mockDelay)

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
            ByteArray(0)
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
            ByteArray(0)
        )
    }

}
