package com.simprints.infra.eventsync.event.remote

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class ApiExternalCredentialType {
    NHIS_CARD,
    GHANA_CARD,
    QR_CODE,
    FAYDA_CARD,
    ;

    fun toDomain(): ExternalCredentialType = when (this) {
        NHIS_CARD -> ExternalCredentialType.NHISCard
        GHANA_CARD -> ExternalCredentialType.GhanaIdCard
        QR_CODE -> ExternalCredentialType.QRCode
        FAYDA_CARD -> ExternalCredentialType.FaydaCard
        // ICAO_DOCUMENT -> ExternalCredentialType.IcaoDocument
    }
}

fun ExternalCredentialType.fromDomainToApi(): ApiExternalCredentialType = when (this) {
    ExternalCredentialType.NHISCard -> ApiExternalCredentialType.NHIS_CARD
    ExternalCredentialType.GhanaIdCard -> ApiExternalCredentialType.GHANA_CARD
    ExternalCredentialType.QRCode -> ApiExternalCredentialType.QR_CODE
    ExternalCredentialType.FaydaCard -> ApiExternalCredentialType.FAYDA_CARD
    ExternalCredentialType.IcaoDocument -> ApiExternalCredentialType.GHANA_CARD // TODO, PoC - masking ICAO as Ghana card for now
    // ExternalCredentialType.IcaoDocument -> ApiExternalCredentialType.ICAO_DOCUMENT
}
