package com.simprints.feature.externalcredential.screens.scandocument

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadFailure
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadResult
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzData

@ExcludedFromGeneratedTestCoverageReports("State class")
internal sealed class DocumentScanState {
    data object NotInitialised : DocumentScanState()

    data class NoCameraPermission(
        val shouldOpenPhoneSettings: Boolean,
    ) : DocumentScanState()

    /** Camera is running, looking for a valid MRZ */
    data object ScanningMrz : DocumentScanState()

    /** MRZ is read, waiting for the document chip to be tapped */
    data class WaitingForChip(
        val mrz: MrzData,
        val lastFailure: ChipReadFailure? = null,
    ) : DocumentScanState()

    data class ReadingChip(
        val mrz: MrzData,
        val attempt: Int,
    ) : DocumentScanState()

    data class ChipRead(
        val mrz: MrzData,
        val chip: ChipReadResult,
        val scanStartTime: Timestamp,
        val scanEndTime: Timestamp,
    ) : DocumentScanState()
}
