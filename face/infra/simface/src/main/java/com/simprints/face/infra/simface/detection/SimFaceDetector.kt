package com.simprints.face.infra.simface.detection

import android.graphics.Bitmap
import com.simprints.biometrics.simpalm.SimPalm
import com.simprints.biometrics.simpalm.detection.PalmDetection
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class SimFaceDetector @Inject constructor(
    private val simPalm: SimPalm,
) : FaceDetector {
    override fun analyze(bitmap: Bitmap): Face? = runBlocking {
        // Load a bitmap image for processing
        val palms = simPalm.detectPalmBlocking(bitmap)
        val palm = palms.getOrNull(0) ?: return@runBlocking null

        // Skip the obviously bad images using the SimQ score,
        // but leave the rest to be determined by the caller
        if (palm.quality < BAD_FACE_THRESHOLD) return@runBlocking null

        // Extract the cropped palm image from the original bitmap
        // Note: Returns empty bytes until a palm identity model is fully implemented
        val template = simPalm.getEmbedding(bitmap)
        Face(
            sourceWidth = bitmap.width,
            sourceHeight = bitmap.height,
            absoluteBoundingBox = palm.boundingBox,
            yaw = 0f,
            roll = 0f,
            quality = palm.quality,
            template = template,
            format = simPalm.getTemplateVersion(),
            bitmap = palm.bitmap,
            isFlipped = false, // palm.handedness == Handedness.FLIPPED,
        )
    }

    private fun PalmDetection.croppedPalmImage(originalImage: Bitmap) = Bitmap.createBitmap(
        originalImage,
        boundingBox.left.coerceAtLeast(0),
        boundingBox.top.coerceAtLeast(0),
        boundingBox.width().coerceAtMost(originalImage.width - boundingBox.left),
        boundingBox.height().coerceAtMost(originalImage.height - boundingBox.top),
    )

    companion object {
        private const val BAD_FACE_THRESHOLD = 0.6
    }
}
