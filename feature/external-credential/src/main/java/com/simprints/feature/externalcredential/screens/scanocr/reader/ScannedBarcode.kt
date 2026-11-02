package com.simprints.feature.externalcredential.screens.scanocr.reader

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.externalcredential.model.BoundingBox

/**
 * A single barcode detected in the scanned image.
 *
 * @param value decoded barcode value
 * @param boundingBox coordinates of the barcode
 */
@ExcludedFromGeneratedTestCoverageReports("Data class")
internal data class ScannedBarcode(
    val value: String,
    val boundingBox: BoundingBox,
)
