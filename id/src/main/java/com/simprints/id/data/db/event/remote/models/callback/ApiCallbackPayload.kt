package com.simprints.id.data.db.event.remote.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.CALLBACK
import com.simprints.id.data.db.event.remote.models.callback.ApiCallbackType.*
import com.simprints.id.data.db.event.remote.models.fromApiToDomain

@Keep
class ApiCallbackPayload(
    override val relativeStartTime: Long,
    override val version: Int,
    val callback: ApiCallback
) : ApiEventPayload(CALLBACK, version, relativeStartTime) {

    constructor(domainPayload: EnrolmentCallbackPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        ApiEnrolmentCallback(domainPayload.guid)
    )

    constructor(domainPayload: IdentificationCallbackPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        ApiIdentificationCallback(domainPayload.sessionId, domainPayload.scores.map { it.fromDomainToApi() })
    )

    constructor(domainPayload: VerificationCallbackPayload) : this(
        domainPayload.createdAt,
        domainPayload.eventVersion,
        ApiVerificationCallback(domainPayload.score.fromDomainToApi()))

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

fun ApiCallbackPayload.fromApiToDomain(): EventPayload =
    when (this.callback.type) {
        ENROLMENT -> (callback as ApiEnrolmentCallback).let { EnrolmentCallbackPayload(relativeStartTime, version, it.guid, type.fromApiToDomain(), 0) }
        IDENTIFICATION -> (callback as ApiIdentificationCallback).let { IdentificationCallbackPayload(relativeStartTime, version, it.sessionId, it.scores.map { it.fromApiToDomain() }) }
        REFUSAL -> (callback as ApiRefusalCallback).let { RefusalCallbackPayload(relativeStartTime, version, it.reason, it.extra) }
        VERIFICATION -> (callback as ApiVerificationCallback).let { VerificationCallbackPayload(relativeStartTime, version, it.score.fromApiToDomain()) }
        ERROR -> (callback as ApiErrorCallback).let { ErrorCallbackPayload(relativeStartTime, version, it.reason.fromApiToDomain()) }
        CONFIRMATION -> (callback as ApiConfirmationCallback).let { ConfirmationCallbackPayload(relativeStartTime, version, it.received) }
    }
