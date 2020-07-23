package com.simprints.id.data.db.event.remote.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.id.data.db.event.remote.events.ApiEvent
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType.CALLBACK
import com.simprints.id.data.db.event.remote.events.fromDomainToApi

@Keep
class ApiCallbackEvent(domainEvent: Event) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiCallbackPayload(
        createdAt: Long,
        version: Int,
        val callout: ApiCallback
    ) : ApiEventPayload(CALLBACK, version, createdAt) {

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
}

