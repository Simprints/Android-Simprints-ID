package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType.FAYDA_CARD
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType.GHANA_CARD
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType.NHIS_CARD
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType.QR_CODE

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
    FAYDA_CARD,
    QR_CODE,
    ;

    fun toDomain(): ExternalCredentialType = when (this) {
        NHIS_CARD -> ExternalCredentialType.NHISCard
        GHANA_CARD -> ExternalCredentialType.GhanaIdCard
        FAYDA_CARD -> ExternalCredentialType.FaydaCard
        QR_CODE -> ExternalCredentialType.QRCode
    }
}

fun ExternalCredentialType.fromDomainToApi(): ApiExternalCredentialType = when (this) {
    ExternalCredentialType.NHISCard -> NHIS_CARD
    ExternalCredentialType.GhanaIdCard -> GHANA_CARD
    ExternalCredentialType.FaydaCard -> FAYDA_CARD
    ExternalCredentialType.QRCode -> QR_CODE
}
