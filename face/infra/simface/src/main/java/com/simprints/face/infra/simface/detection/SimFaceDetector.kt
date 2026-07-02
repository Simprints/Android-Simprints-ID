package com.simprints.face.infra.simface.detection

import android.graphics.Bitmap
import com.simprints.biometrics.simface.SimFace
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.face.infra.basebiosdk.detection.Face.Companion.IOD_NOT_AVAILABLE
import com.simprints.face.infra.basebiosdk.detection.FaceDetector
import com.simprints.face.infra.basebiosdk.detection.SpoofCheckResult
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.math.abs

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
            iod = face.landmarks?.let { abs(it.eyeLeft.x - it.eyeRight.x) } ?: IOD_NOT_AVAILABLE,
            quality = face.quality,
            template = template,
            format = simFace.getTemplateVersion(),
        )
    }

    override fun runSpoofCheck(bitmap: Bitmap) = SpoofCheckResult(0f, SpoofCheckResult.SkipReason.NOT_AVAILABLE)

    companion object {
        private const val BAD_FACE_THRESHOLD = 0.1
    }
}
