package com.simprints.infra.eventsync.event.remote.models.subject

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted

data class ApiExternalCredential(
    val id: String,
    val type: String,
    val value: String,
)


internal fun ApiExternalCredential.fromApiToDomain(subjectId: String) = ExternalCredential(
    value = value.asTokenizableEncrypted(),
    subjectId = subjectId,
    type = ExternalCredentialType.valueOf(type)
)
