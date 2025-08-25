package com.simprints.core.domain.externalcredential

import android.os.Parcelable
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.parcelize.Parcelize

@Parcelize
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
data class ExternalCredential(
    val value: TokenizableString.Tokenized,
    val subjectId: String,
    val type: ExternalCredentialType,
) : Parcelable
