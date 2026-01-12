package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.ExternalCredentialConfirmationEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialConfirmationEvent.ExternalCredentialConfirmationResult
import com.simprints.infra.eventsync.event.remote.models.ApiExternalCredentialConfirmationPayload.ApiExternalCredentialConfirmationResult
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiExternalCredentialConfirmationPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val result: ApiExternalCredentialConfirmationResult,
    val userInteractedWithImage: Boolean? = null,
) : ApiEventPayload() {
    constructor(domainPayload: ExternalCredentialConfirmationEvent.ExternalCredentialConfirmationPayload) : this(
        startTime = domainPayload.createdAt.fromDomainToApi(),
        endTime = domainPayload.endedAt?.fromDomainToApi(),
        result = domainPayload.result.fromDomainToApi(),
        userInteractedWithImage = domainPayload.userInteractedWithImage,
    )

    @Keep
    @Serializable
    enum class ApiExternalCredentialConfirmationResult {
        CONTINUE,
        RECAPTURE,
    }

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null
}

internal fun ExternalCredentialConfirmationResult.fromDomainToApi(): ApiExternalCredentialConfirmationResult = when (this) {
    ExternalCredentialConfirmationResult.CONTINUE -> ApiExternalCredentialConfirmationResult.CONTINUE
    ExternalCredentialConfirmationResult.RECAPTURE -> ApiExternalCredentialConfirmationResult.RECAPTURE
}
