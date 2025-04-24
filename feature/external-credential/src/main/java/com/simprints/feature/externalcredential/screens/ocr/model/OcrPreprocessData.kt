package com.simprints.feature.externalcredential.screens.ocr.model

import android.graphics.RectF

/**
 * Data class that contains all necessary information about preview views on the screen when user takes the picture for
 * OCR processing.
 * We want to pre-process image before applying the OCR within the ViewModel's scope, but this means it's necessary to avoid
 * direct references to the views. Thus, capturing all the necessary preprocessing data here
 */
data class OcrPreprocessData(
    val previewViewWidthPx: Int,
    val previewViewHeightPx: Int,
    val cutoutRect: RectF
)
