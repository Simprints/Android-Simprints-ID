package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureEvent.ExternalCredentialCapturePayload

@Keep
internal data class ApiExternalCredentialCapturePayload(
    override val startTime: ApiTimestamp,
    val id: String,
    val endTime: ApiTimestamp?,
    val autoCaptureStartTime: ApiTimestamp,
    val autoCaptureEndTime: ApiTimestamp,
    val ocrErrorCount: Int,
    val capturedTextLength: Int,
    val credentialTextLength: Int,
    val selectionId: String,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: ExternalCredentialCapturePayload) : this(
        startTime = domainPayload.startTime.fromDomainToApi(),
        endTime = domainPayload.endTime?.fromDomainToApi(),
        id = domainPayload.id,
        autoCaptureStartTime = domainPayload.autoCaptureStartTime.fromDomainToApi(),
        autoCaptureEndTime = domainPayload.autoCaptureEndTime.fromDomainToApi(),
        ocrErrorCount = domainPayload.ocrErrorCount,
        capturedTextLength = domainPayload.capturedTextLength,
        credentialTextLength = domainPayload.credentialTextLength,
        selectionId = domainPayload.selectionId,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null
}
