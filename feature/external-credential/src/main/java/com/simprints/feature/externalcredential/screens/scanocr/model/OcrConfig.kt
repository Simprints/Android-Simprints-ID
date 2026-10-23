package com.simprints.feature.externalcredential.screens.scanocr.model

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("Data struct")
internal data class OcrConfig(
    val useHighRes: Boolean,
    val keepHighResDetail: Boolean,
    val capturesRequired: Int,
    val readFaydaBarcode: Boolean,
)
