package com.simprints.ear.infra.earsdk

import android.graphics.Bitmap
import android.graphics.Rect
import com.simprints.ear.infra.basebiosdk.detection.Ear
import com.simprints.ear.infra.basebiosdk.detection.EarDetector
import com.simprints.simface.core.SimFaceFacade
import javax.inject.Inject

class EarSimFaceDetector @Inject constructor(
    private val simFace: SimFaceFacade,
) : EarDetector {
    companion object {
        const val EAR_SIM_FACE_TEMPLATE = "EAR_SIM_FACE"
    }

    override fun analyze(bitmap: Bitmap): Ear? {
        // Generate an embedding from the image
        val template = simFace.embeddingProcessor.getEmbedding(bitmap)

        return Ear(
            sourceHeight = bitmap.height,
            sourceWidth = bitmap.width,
            absoluteBoundingBox = bitmap.let {
                Rect(0, 0, it.width, it.height)
            },
            template = template,
            quality = 1f,
            format = EAR_SIM_FACE_TEMPLATE,
        )
    }
}
