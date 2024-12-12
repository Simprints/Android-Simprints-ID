package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiCallbackPayload(
    override val startTime: ApiTimestamp,
    val callback: ApiCallback,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: EnrolmentCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiEnrolmentCallback(domainPayload.guid),
    )

    constructor(domainPayload: IdentificationCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiIdentificationCallback(
            domainPayload.sessionId,
            domainPayload.scores.map { it.fromDomainToApi(domainPayload.eventVersion) },
        ),
    )

    constructor(domainPayload: VerificationCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiVerificationCallback(domainPayload.score.fromDomainToApi(domainPayload.eventVersion)),
    )

    constructor(domainPayload: ConfirmationCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiConfirmationCallback(domainPayload.identificationOutcome),
    )

    constructor(domainPayload: ErrorCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiErrorCallback(domainPayload.reason.fromDomainToApi()),
    )

    constructor(domainPayload: RefusalCallbackPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        ApiRefusalCallback(domainPayload.reason, domainPayload.extra),
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
}
