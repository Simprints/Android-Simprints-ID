package com.simprints.core.domain.externalcredential

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.tokenization.TokenizableString
import java.io.Serializable

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class ExternalCredential(
    val id: String,
    val value: TokenizableString.Tokenized,
    val subjectId: String,
    val type: ExternalCredentialType,
) : Serializable
