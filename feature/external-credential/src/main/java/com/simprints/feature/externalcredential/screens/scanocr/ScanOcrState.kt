package com.simprints.feature.externalcredential.screens.scanocr

import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType


internal sealed class ScanOcrState {
    data object NotScanning : ScanOcrState()
    data class ScanningInProgress(
        val ocrDocumentType: OcrDocumentType,
        val successfulCaptures: Int,
        val scansRequired: Int,
    ) : ScanOcrState()
    companion object {
        val EMPTY = ScanOcrState.NotScanning
    }
}
