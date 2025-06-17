package com.simprints.face.infra.simface.detection

import android.graphics.Bitmap
import com.simprints.biometrics.simface.SimFace
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class SimFaceDetector @Inject constructor(
    private val simFace: SimFace,
) : FaceDetector {
    override fun analyze(bitmap: Bitmap): Face? = runBlocking {
        Simber.d("vvvvvvvvvvvvvvvvvvv", tag = "FACE_DETECTOR")

        // Load a bitmap image for processing
        val faces = simFace.detectFaceBlocking(bitmap)
        Simber.d("Faces found: ${faces.size}", tag = "FACE_DETECTOR")

        val face = faces.getOrNull(0) ?: return@runBlocking null
        // Skip the obviously bad images, but leave the rest to be determined by the caller

        Simber.d("Quality: ${face.quality} Box: ${face.absoluteBoundingBox}", tag = "FACE_DETECTOR")
        if (face.quality < BAD_FACE_THRESHOLD) return@runBlocking null

        val alignedBitmap = face.alignedFaceImage(bitmap)
        val template = simFace.getEmbedding(alignedBitmap)

        Simber.d("Yaw: ${face.yaw} Roll: ${face.roll}", tag = "FACE_DETECTOR")
        Simber.d("Original size: ${bitmap.width}x${bitmap.height}", tag = "FACE_DETECTOR")
        Simber.d("Aligned size: ${alignedBitmap.width}x${alignedBitmap.height}", tag = "FACE_DETECTOR")
        Simber.d("^^^^^^^^^^^^^^^^^^^^", tag = "FACE_DETECTOR")

        Face(
            sourceWidth = bitmap.width,
            sourceHeight = bitmap.height,
            absoluteBoundingBox = face.absoluteBoundingBox,
            yaw = face.yaw,
            roll = face.roll,
            quality = face.quality,
            template = template,
            format = simFace.getTemplateVersion(),
        )
    }

    companion object {
        private const val BAD_FACE_THRESHOLD = 0.1
    }
}
