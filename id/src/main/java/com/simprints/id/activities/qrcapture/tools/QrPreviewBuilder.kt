package com.simprints.id.activities.qrcapture.tools

import androidx.camera.core.Preview
import androidx.camera.view.PreviewView

interface QrPreviewBuilder {
    fun buildPreview(previewView: PreviewView): Preview
}
