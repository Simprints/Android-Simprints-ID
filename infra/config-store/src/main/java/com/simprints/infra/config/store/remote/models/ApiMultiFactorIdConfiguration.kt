package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration

@Keep
internal data class ApiMultiFactorIdConfiguration(
    val allowedExternalCredentials: List<ApiExternalCredentialType>
) {
    fun toDomain(): MultiFactorIdConfiguration = MultiFactorIdConfiguration(
        allowedExternalCredentials = allowedExternalCredentials.map { it.toDomain() }
    )
}

@Keep
enum class ApiExternalCredentialType {
    NHIS_CARD, GhanaIdCard, QRCode;

    fun toDomain(): ExternalCredentialType = when (this) {
        NHIS_CARD -> ExternalCredentialType.NHISCard
        GhanaIdCard -> ExternalCredentialType.GhanaIdCard
        QRCode -> ExternalCredentialType.QRCode
    }
}
