package com.simprints.ear.infra.earsdk

import android.graphics.Bitmap
import com.simprints.ear.infra.basebiosdk.detection.Ear
import com.simprints.ear.infra.basebiosdk.detection.EarDetector
import com.simprints.simface.core.SimFaceFacade
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class EarSimFaceDetector @Inject constructor(
    private val simFace: SimFaceFacade
) : EarDetector {

    companion object {
        const val EAR_SIM_FACE_TEMPLATE = "EAR_SIM_FACE"
    }

    override fun analyze(bitmap: Bitmap): Ear? {
        val faces = runBlocking { simFace.faceDetectionProcessor.detectFaceBlocking(bitmap) }
        val face = faces[0]

        if (faces.size != 1 || face.quality < 0.6) return null

        // Align and crop the image to the face bounding box
        val alignedFace = simFace.faceDetectionProcessor.alignFace(bitmap, face.absoluteBoundingBox)

        // Generate an embedding from the image
        val template = simFace.embeddingProcessor.getEmbedding(alignedFace)

        return Ear(
            sourceHeight = face.sourceHeight,
            sourceWidth = face.sourceWidth,
            template = template,
            quality = face.quality,
            format = EAR_SIM_FACE_TEMPLATE,
        )
    }
}
