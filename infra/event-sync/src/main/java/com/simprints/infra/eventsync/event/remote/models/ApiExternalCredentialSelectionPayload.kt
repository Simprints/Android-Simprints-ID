package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.remote.models.ApiExternalCredentialType
import com.simprints.infra.config.store.remote.models.fromDomainToApi
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent.SkipReason
import com.simprints.infra.eventsync.event.remote.models.ApiExternalCredentialSelectionPayload.ApiExternalCredentialSkipReason

@Keep
internal data class ApiExternalCredentialSelectionPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val id: String,
    val credentialType: ApiExternalCredentialType?,
    val skipReason: ApiExternalCredentialSkipReason?,
    val skipOther: String?,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: ExternalCredentialSelectionEvent.ExternalCredentialSelectionPayload) : this(
        startTime = domainPayload.createdAt.fromDomainToApi(),
        endTime = domainPayload.endedAt?.fromDomainToApi(),
        id = domainPayload.id,
        credentialType = domainPayload.credentialType?.fromDomainToApi(),
        skipReason = domainPayload.skipReason?.toApiExternalCredentialSkipReason(),
        skipOther = domainPayload.skipOther,
    )

    @Keep
    enum class ApiExternalCredentialSkipReason {
        DOES_NOT_HAVE_ID,
        DID_NOT_BRING_ID,
        BROUGHT_INCORRECT_ID,
        NO_CONSENT,
        ID_DAMAGED,
        UNABLE_TO_SCAN,
        OTHER,
    }

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null
}

internal fun SkipReason.toApiExternalCredentialSkipReason(): ApiExternalCredentialSkipReason = when (this) {
    SkipReason.DOES_NOT_HAVE_ID -> ApiExternalCredentialSkipReason.DOES_NOT_HAVE_ID
    SkipReason.DID_NOT_BRING_ID -> ApiExternalCredentialSkipReason.DID_NOT_BRING_ID
    SkipReason.BROUGHT_INCORRECT_ID -> ApiExternalCredentialSkipReason.BROUGHT_INCORRECT_ID
    SkipReason.NO_CONSENT -> ApiExternalCredentialSkipReason.NO_CONSENT
    SkipReason.ID_DAMAGED -> ApiExternalCredentialSkipReason.ID_DAMAGED
    SkipReason.UNABLE_TO_SCAN -> ApiExternalCredentialSkipReason.UNABLE_TO_SCAN
    SkipReason.OTHER -> ApiExternalCredentialSkipReason.OTHER
}
