package com.simprints.infra.orchestration.data.responses

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.TokenizableString
import java.io.Serializable

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class")
data class AppExternalCredential(
    val id: String,
    val value: TokenizableString.Raw,
    val type: ExternalCredentialType,
) : Serializable
