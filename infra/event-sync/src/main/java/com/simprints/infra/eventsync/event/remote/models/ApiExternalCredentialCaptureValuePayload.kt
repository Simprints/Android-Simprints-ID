package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent.ExternalCredentialCaptureValuePayload
import com.simprints.infra.eventsync.event.remote.ApiExternalCredential
import com.simprints.infra.eventsync.event.remote.fromDomainToApi
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiExternalCredentialCaptureValuePayload(
    override val startTime: ApiTimestamp,
    val id: String,
    val credential: ApiExternalCredential,
) : ApiEventPayload() {
    constructor(domainPayload: ExternalCredentialCaptureValuePayload) : this(
        startTime = domainPayload.createdAt.fromDomainToApi(),
        id = domainPayload.id,
        credential = domainPayload.credential.fromDomainToApi(),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = when (tokenKeyType) {
        TokenKeyType.ExternalCredential -> "credential.value"

        TokenKeyType.AttendantId,
        TokenKeyType.ModuleId,
        TokenKeyType.Unknown,
        -> null
    }
}
