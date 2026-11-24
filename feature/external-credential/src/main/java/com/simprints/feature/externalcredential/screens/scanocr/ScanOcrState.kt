package com.simprints.feature.externalcredential.screens.scanocr

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredentialResult

@ExcludedFromGeneratedTestCoverageReports("Data struct")
internal sealed class ScanOcrState {
    data object NotScanning : ScanOcrState()

    data class ScanningInProgress(
        val maxScansRequired: Int,
    ) : ScanOcrState()

    data object ScansProcessing : ScanOcrState()

    data class ScansReady(
        val result: ScannedCredentialResult,
    ) : ScanOcrState()
}
