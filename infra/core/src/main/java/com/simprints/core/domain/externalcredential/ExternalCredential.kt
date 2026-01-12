package com.simprints.core.domain.externalcredential

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Keep
@ExcludedFromGeneratedTestCoverageReports("Data class with generated code")
@Serializable
@Parcelize
data class ExternalCredential(
    val id: String,
    val value: TokenizableString.Tokenized,
    val subjectId: String,
    val type: ExternalCredentialType,
) : Parcelable
