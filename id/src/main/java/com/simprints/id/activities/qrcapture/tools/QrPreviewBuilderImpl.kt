package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.AspectRatio
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView

class QrPreviewBuilderImpl : QrPreviewBuilder {

    override fun buildPreview(previewView: PreviewView): Preview {
        return Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build().apply {
            setSurfaceProvider(previewView.previewSurfaceProvider)
        }
    }

}
