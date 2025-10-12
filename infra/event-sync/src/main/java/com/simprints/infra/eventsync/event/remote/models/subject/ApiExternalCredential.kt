package com.simprints.infra.eventsync.event.remote.models.subject

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType

data class ApiExternalCredential(
    val id: String,
    val type: ApiExternalCredentialType,
    val value: String,
)

internal fun ApiExternalCredential.fromApiToDomain(subjectId: String) = ExternalCredential(
    id = id,
    value = value.asTokenizableEncrypted(),
    subjectId = subjectId,
    type = type.toDomain(),
)

internal fun ExternalCredential.fromDomainToApi() = ApiExternalCredential(
    id = id,
    value = value.value,
    type = when (type) {
        ExternalCredentialType.NHISCard -> ApiExternalCredentialType.NHIS_CARD
        ExternalCredentialType.GhanaIdCard -> ApiExternalCredentialType.GHANA_CARD
        ExternalCredentialType.QRCode -> ApiExternalCredentialType.QR_CODE
    },
)
