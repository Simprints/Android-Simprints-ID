package com.simprints.feature.externalcredential.screens.ocr.model

import android.graphics.Bitmap

sealed class OcrPreviewState {

    data object Initial : OcrPreviewState()
    data object Loading : OcrPreviewState()

    data class Success(
        val preprocessedImage: Bitmap,
    ) : OcrPreviewState()

    data class Error(
        val message: String,
    ) : OcrPreviewState()

}
