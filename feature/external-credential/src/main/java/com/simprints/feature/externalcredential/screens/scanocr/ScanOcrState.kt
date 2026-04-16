package com.simprints.feature.externalcredential.screens.scanocr

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType

@ExcludedFromGeneratedTestCoverageReports("Data struct")
internal sealed class ScanOcrState {
    data object NotScanning : ScanOcrState()

    data class ScanningInProgress(
        val ocrDocumentType: OcrDocumentType,
        val successfulCaptures: Int,
        val scansRequired: Int,
    ) : ScanOcrState()

    data object Complete : ScanOcrState()

    companion object {
        val EMPTY = ScanOcrState.NotScanning
    }
}
