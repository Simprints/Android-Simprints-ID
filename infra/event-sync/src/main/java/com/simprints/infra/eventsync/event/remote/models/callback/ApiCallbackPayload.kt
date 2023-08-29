package com.simprints.infra.eventsync.event.remote.models.callback

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.infra.events.event.domain.models.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType.Callback
import com.simprints.infra.eventsync.event.remote.models.callback.*
import com.simprints.infra.eventsync.event.remote.models.callback.ApiCallbackType.*
import com.simprints.infra.eventsync.event.remote.models.fromApiToDomain

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiCallbackPayload(
    override val startTime: Long,
    override val version: Int,
    val callback: ApiCallback
) : ApiEventPayload(Callback, version, startTime) {

    constructor(domainPayload: EnrolmentCallbackPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        ApiEnrolmentCallback(domainPayload.guid)
    )

    constructor(domainPayload: IdentificationCallbackPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        ApiIdentificationCallback(domainPayload.sessionId, domainPayload.scores.map { it.fromDomainToApi(domainPayload.eventVersion) })
    )

    constructor(domainPayload: VerificationCallbackPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        ApiVerificationCallback(domainPayload.score.fromDomainToApi(domainPayload.eventVersion)))

    constructor(domainPayload: ConfirmationCallbackPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        ApiConfirmationCallback(domainPayload.identificationOutcome))

    constructor(domainPayload: ErrorCallbackPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        ApiErrorCallback(domainPayload.reason.fromDomainToApi()))

    constructor(domainPayload: RefusalCallbackPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        ApiRefusalCallback(domainPayload.reason, domainPayload.extra))
}
