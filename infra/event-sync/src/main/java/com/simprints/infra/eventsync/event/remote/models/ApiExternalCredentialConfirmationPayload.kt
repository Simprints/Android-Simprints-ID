package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.ExternalCredentialConfirmationEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialConfirmationEvent.ExternalCredentialConfirmationResult
import com.simprints.infra.eventsync.event.remote.models.ApiExternalCredentialConfirmationPayload.ApiExternalCredentialConfirmationResult

@Keep
internal data class ApiExternalCredentialConfirmationPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val result: ApiExternalCredentialConfirmationResult,
    val userInteractedWithImage: Boolean? = null,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: ExternalCredentialConfirmationEvent.ExternalCredentialConfirmationPayload) : this(
        startTime = domainPayload.startTime.fromDomainToApi(),
        endTime = domainPayload.endTime?.fromDomainToApi(),
        result = domainPayload.result.fromDomainToApi(),
        userInteractedWithImage = domainPayload.userInteractedWithImage,
    )

    @Keep
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
