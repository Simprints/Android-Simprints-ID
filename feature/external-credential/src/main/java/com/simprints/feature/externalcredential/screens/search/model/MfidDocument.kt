package com.simprints.feature.externalcredential.screens.search.model

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.step.StepResult
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@ExcludedFromGeneratedTestCoverageReports("Data class")
sealed class MfidDocument : StepResult {
    abstract val credential: TokenizableString.Raw

    @Serializable
    @SerialName("MfidDocument.GhanaNhisCard")
    data class GhanaNhisCard(
        override val credential: TokenizableString.Raw,
        val name: TokenizableString.Raw? = null,
        val dateOfBirth: TokenizableString.Raw? = null,
        val sex: TokenizableString.Raw? = null,
        val dateOfIssue: TokenizableString.Raw? = null,
    ) : MfidDocument()

    @Serializable
    @SerialName("MfidDocument.GhanaIdCard")
    data class GhanaIdCard(
        override val credential: TokenizableString.Raw,
        val surname: TokenizableString.Raw? = null,
        val firstName: TokenizableString.Raw? = null,
        val nationality: TokenizableString.Raw? = null,
        val dateOfBirth: TokenizableString.Raw? = null,
        val height: TokenizableString.Raw? = null,
        val documentNumber: TokenizableString.Raw? = null,
        val placeOfIssue: TokenizableString.Raw? = null,
        val dateOfIssue: TokenizableString.Raw? = null,
        val dateOfExpiry: TokenizableString.Raw? = null,
    ) : MfidDocument()

    @Serializable
    @SerialName("MfidDocument.GhanaQrCode")
    data class GhanaQrCode(
        override val credential: TokenizableString.Raw,
    ) : MfidDocument()

    @Serializable
    @SerialName("MfidDocument.FaydaCard")
    data class FaydaCard(
        override val credential: TokenizableString.Raw,
    ) : MfidDocument()
}
