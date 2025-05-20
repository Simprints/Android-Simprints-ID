package com.simprints.feature.externalcredential.model

import androidx.annotation.Keep
import java.io.Serializable


sealed class ExternalCredentialResponse : Serializable {

    abstract val externalCredential: String?
    abstract val imagePreviewPath: String?

    @Keep
    data class ExternalCredentialSaveResponse(
        val subjectId: String,
        override val externalCredential: String,
        override val imagePreviewPath: String?,
    ) : ExternalCredentialResponse()

    @Keep
    data class ExternalCredentialSearchResponse(
        val subjectId: String?,
        override val externalCredential: String,
        override val imagePreviewPath: String?,
    ) : ExternalCredentialResponse()

    @Keep
    data class ExternalCredentialSkipResponse(
        override val externalCredential: String? = null,
        override val imagePreviewPath: String? = null,
    ) : ExternalCredentialResponse()
}
