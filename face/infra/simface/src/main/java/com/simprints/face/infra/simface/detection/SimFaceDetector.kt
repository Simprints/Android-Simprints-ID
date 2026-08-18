package com.simprints.face.infra.simface.detection

import android.graphics.Bitmap
import com.simprints.biometrics.simface.SimFace
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.detection.SpoofCheckResult
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class SimFaceDetector @Inject constructor(
    private val simFace: SimFace,
) : FaceDetector {
    // Overload preserving source compatibility with the previous 1-arg API for direct callers
    // of this concrete type (the interface already defaults `estimateAgeAndGender` to false).
    fun analyze(bitmap: Bitmap): Face? = analyze(bitmap, estimateAgeAndGender = false)

    override fun analyze(
        bitmap: Bitmap,
        estimateAgeAndGender: Boolean,
    ): Face? = runBlocking {
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

    override fun spoofCheck(
        bitmap: Bitmap,
        configuredMaxSize: Int,
    ) = SpoofCheckResult(0f, SpoofCheckResult.SkipReason.NOT_AVAILABLE)

    companion object {
        private const val BAD_FACE_THRESHOLD = 0.1
    }
}
