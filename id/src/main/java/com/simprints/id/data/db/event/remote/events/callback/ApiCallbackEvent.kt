package com.simprints.id.data.db.event.remote.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.id.data.db.event.remote.events.ApiEvent
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType.CALLBACK
import com.simprints.id.data.db.event.remote.events.fromDomainToApi

@Keep
class ApiCallbackEvent(id: String,
                       labels: List<Event.EventLabel>,
                       payload: EventPayload) :
    ApiEvent(id, labels.fromDomainToApi(), payload.fromDomainToApi()) {

    @Keep
    class ApiCallbackPayload(
        creationTime: Long,
        eventVersion: Int,
        val callout: ApiCallback
    ) : ApiEventPayload(CALLBACK, eventVersion, creationTime) {

        constructor(domainPayload: EnrolmentCallbackPayload) : this(
            domainPayload.creationTime,
            domainPayload.eventVersion,
            ApiEnrolmentCallback(domainPayload.guid)
        )

        constructor(domainPayload: IdentificationCallbackPayload) : this(
            domainPayload.creationTime,
            domainPayload.eventVersion,
            ApiIdentificationCallback(domainPayload.sessionId, domainPayload.scores.map { it.fromDomainToApi() })
        )

        constructor(domainPayload: VerificationCallbackPayload) : this(
            domainPayload.creationTime,
            domainPayload.eventVersion,
            ApiVerificationCallback(domainPayload.score.fromDomainToApi()))

        constructor(domainPayload: ConfirmationCallbackPayload) : this(
            domainPayload.creationTime,
            domainPayload.eventVersion,
            ApiConfirmationCallback(domainPayload.identificationOutcome))

        constructor(domainPayload: ErrorCallbackPayload) : this(
            domainPayload.creationTime,
            domainPayload.eventVersion,
            ApiErrorCallback(domainPayload.reason.fromDomainToApi()))

        constructor(domainPayload: RefusalCallbackPayload) : this(
            domainPayload.creationTime,
            domainPayload.eventVersion,
            ApiRefusalCallback(domainPayload.reason, domainPayload.extra))
    }
}

