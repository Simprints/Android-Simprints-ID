package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration

@Keep
internal data class ApiMultiFactorIdConfiguration(
    val allowedExternalCredentials: List<ApiExternalCredentialType>,
) {
    fun toDomain(): MultiFactorIdConfiguration = MultiFactorIdConfiguration(
        allowedExternalCredentials = allowedExternalCredentials.map { it.toDomain() },
    )
}

@Keep
enum class ApiExternalCredentialType {
    NHIS_CARD,
    GHANA_CARD,
    QR_CODE,
    ;

    fun toDomain(): ExternalCredentialType = when (this) {
        NHIS_CARD -> ExternalCredentialType.NHISCard
        GHANA_CARD -> ExternalCredentialType.GhanaIdCard
        QR_CODE -> ExternalCredentialType.QRCode
    }
}
