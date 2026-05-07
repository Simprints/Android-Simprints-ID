package com.simprints.feature.externalcredential.screens.scanocr.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrLine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@ExcludedFromGeneratedTestCoverageReports("Data class")
internal sealed class OcrScanResult {
    abstract val credential: OcrLine

    @Serializable
    @SerialName("OcrScanResult.GhanaNhisCard")
    data class GhanaNhisCard(
        override val credential: OcrLine,
        val name: OcrLine? = null,
        val dateOfBirth: OcrLine? = null,
        val sex: OcrLine? = null,
        val dateOfIssue: OcrLine? = null,
    ) : OcrScanResult()

    @Serializable
    @SerialName("OcrScanResult.GhanaIdCard")
    data class GhanaIdCard(
        override val credential: OcrLine,
        val surname: OcrLine? = null,
        val firstName: OcrLine? = null,
        val nationality: OcrLine? = null,
        val dateOfBirth: OcrLine? = null,
        val height: OcrLine? = null,
        val documentNumber: OcrLine? = null,
        val placeOfIssue: OcrLine? = null,
        val dateOfIssue: OcrLine? = null,
        val dateOfExpiry: OcrLine? = null,
    ) : OcrScanResult()
}
