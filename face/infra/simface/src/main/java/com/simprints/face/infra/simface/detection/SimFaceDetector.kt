package com.simprints.face.infra.simface.detection

import android.graphics.Bitmap
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.simface.core.SimFace
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class SimFaceDetector @Inject constructor(
    private val simFace: SimFace,
) : FaceDetector {
    override fun analyze(bitmap: Bitmap): Face? = runBlocking {
        // Load a bitmap image for processing
        val faces = simFace.detectFaceBlocking(bitmap)
        val face = faces.getOrNull(0) ?: return@runBlocking null
        // Skip the obviously bad images, but leave the rest to be determined by the caller
        if (face.quality < BAD_FACE_THRESHOLD) return@runBlocking null

        val alignedBitmap = face.alignedFaceImage(bitmap)
        val template = simFace.getEmbedding(alignedBitmap)

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
