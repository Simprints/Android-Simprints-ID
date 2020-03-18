package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.AspectRatio
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig

class QrPreviewBuilderImpl : QrPreviewBuilder {

    override fun buildPreview(): Preview {
        val previewConfig = PreviewConfig.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        return Preview(previewConfig)
    }

}
