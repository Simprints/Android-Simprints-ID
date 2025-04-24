package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class ExternalCredentialSaveResponse(
    val subjectId: String,
    val externalCredential: String,
) : Serializable

@Keep
data class ExternalCredentialSearchResponse(
    val subjectId: String?,
    val externalCredential: String,
) : Serializable

@Keep
class ExternalCredentialSkipResponse : Serializable
