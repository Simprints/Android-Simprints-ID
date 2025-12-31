package com.simprints.infra.eventsync.event.remote

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType
import com.simprints.infra.config.store.remote.models.fromDomainToApi
import kotlinx.serialization.Serializable

@Keep
@Serializable
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
    type = type.fromDomainToApi(),
)
