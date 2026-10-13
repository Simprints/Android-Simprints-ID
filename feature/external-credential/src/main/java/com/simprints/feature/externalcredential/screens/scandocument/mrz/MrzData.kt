package com.simprints.feature.externalcredential.screens.scandocument.mrz

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

internal enum class MrzFormat {
    TD1,
    TD2,
    TD3,
}

/**
 * Machine Readable Zone data of an ICAO 9303 document, either read via OCR or from the chip (DG1).
 *
 * Dates are kept in the MRZ "YYMMDD" format as they are used verbatim to derive the chip access key.
 * Contains personal data - must be kept in memory only and never logged or persisted.
 */
@ExcludedFromGeneratedTestCoverageReports("Data class")
internal data class MrzData(
    val format: MrzFormat,
    val documentCode: String,
    val issuingState: String,
    val documentNumber: String,
    val dateOfBirth: String,
    val sex: String,
    val dateOfExpiry: String,
    val nationality: String,
    val primaryIdentifier: String,
    val secondaryIdentifier: String,
    val optionalData1: String,
    val optionalData2: String,
) {
    // Prevents personal data from accidentally leaking into logs or crash reports
    override fun toString(): String = "MrzData(format=$format)"
}
