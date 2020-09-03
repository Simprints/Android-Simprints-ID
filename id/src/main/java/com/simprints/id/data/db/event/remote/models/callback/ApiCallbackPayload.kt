package com.simprints.id.data.db.event.remote.models.callback

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.Callback
import com.simprints.id.data.db.event.remote.models.callback.ApiCallbackType.*
import com.simprints.id.data.db.event.remote.models.fromApiToDomain

@Keep
@JsonInclude(Include.NON_NULL)
data class ApiCallbackPayload(
    override val relativeStartTime: Long,
    override val version: Int,
    val callback: ApiCallback
) : ApiEventPayload(Callback, version, relativeStartTime) {

    constructor(domainPayload: EnrolmentCallbackPayload, baseStartTime: Long) : this(
        domainPayload.createdAt - baseStartTime,
        domainPayload.eventVersion,
        ApiEnrolmentCallback(domainPayload.guid)
    )

    constructor(domainPayload: IdentificationCallbackPayload, baseStartTime: Long) : this(
        domainPayload.createdAt - baseStartTime,
        domainPayload.eventVersion,
        ApiIdentificationCallback(domainPayload.sessionId, domainPayload.scores.map { it.fromDomainToApi() })
    )

    constructor(domainPayload: VerificationCallbackPayload, baseStartTime: Long) : this(
        domainPayload.createdAt - baseStartTime,
        domainPayload.eventVersion,
        ApiVerificationCallback(domainPayload.score.fromDomainToApi()))

    constructor(domainPayload: ConfirmationCallbackPayload, baseStartTime: Long) : this(
        domainPayload.createdAt - baseStartTime,
        domainPayload.eventVersion,
        ApiConfirmationCallback(domainPayload.identificationOutcome))

    constructor(domainPayload: ErrorCallbackPayload, baseStartTime: Long) : this(
        domainPayload.createdAt - baseStartTime,
        domainPayload.eventVersion,
        ApiErrorCallback(domainPayload.reason.fromDomainToApi()))

    constructor(domainPayload: RefusalCallbackPayload, baseStartTime: Long) : this(
        domainPayload.createdAt - baseStartTime,
        domainPayload.eventVersion,
        ApiRefusalCallback(domainPayload.reason, domainPayload.extra))
}

fun ApiCallbackPayload.fromApiToDomain(): EventPayload =
    when (this.callback.type) {
        Enrolment -> (callback as ApiEnrolmentCallback).let { EnrolmentCallbackPayload(relativeStartTime, version, it.guid, type.fromApiToDomain(), 0) }
        Identification -> (callback as ApiIdentificationCallback).let { IdentificationCallbackPayload(relativeStartTime, version, it.sessionId, it.scores.map { it.fromApiToDomain() }) }
        Refusal -> (callback as ApiRefusalCallback).let { RefusalCallbackPayload(relativeStartTime, version, it.reason, it.extra) }
        Verification -> (callback as ApiVerificationCallback).let { VerificationCallbackPayload(relativeStartTime, version, it.score.fromApiToDomain()) }
        Error -> (callback as ApiErrorCallback).let { ErrorCallbackPayload(relativeStartTime, version, it.reason.fromApiToDomain()) }
        Confirmation -> (callback as ApiConfirmationCallback).let { ConfirmationCallbackPayload(relativeStartTime, version, it.received) }
    }
