package com.simprints.infra.config.store.local.models

import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException

internal fun ExternalCredentialType.toProto(): ProtoExternalCredentialType = when(this){
    ExternalCredentialType.NHISCard -> ProtoExternalCredentialType.NHIS_CARD
    ExternalCredentialType.GhanaIdCard -> ProtoExternalCredentialType.GHANA_ID_CARD
    ExternalCredentialType.QRCode -> ProtoExternalCredentialType.QR_CODE
}

internal fun ProtoExternalCredentialType.toDomain(): ExternalCredentialType = when(this){
    ProtoExternalCredentialType.UNRECOGNIZED,
    ProtoExternalCredentialType.EXTERNAL_CREDENTIAL_TYPE_UNSPECIFIED -> throw InvalidProtobufEnumException("invalid External credential $name")
    ProtoExternalCredentialType.NHIS_CARD -> ExternalCredentialType.NHISCard
    ProtoExternalCredentialType.GHANA_ID_CARD -> ExternalCredentialType.GhanaIdCard
    ProtoExternalCredentialType.QR_CODE -> ExternalCredentialType.QRCode
}
