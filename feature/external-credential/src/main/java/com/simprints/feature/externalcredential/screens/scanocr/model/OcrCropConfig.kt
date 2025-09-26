package com.simprints.feature.externalcredential.screens.scanocr.model

import android.graphics.Rect

internal data class OcrCropConfig(
    val rotationDegrees: Int,
    val cutoutRect: Rect,
    val previewViewWidth: Int,
    val previewViewHeight: Int
)
