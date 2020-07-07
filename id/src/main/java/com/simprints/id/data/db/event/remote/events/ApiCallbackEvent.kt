package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.callback.*
import com.simprints.id.data.db.event.domain.events.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.ErrorCallbackEvent.ErrorCallbackEventPayload
import com.simprints.id.data.db.event.domain.events.callback.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.id.data.db.event.domain.events.callback.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.id.data.db.event.remote.events.callback.*

@Keep
class ApiCallbackEvent(val relativeStartTime: Long,
                       val callback: ApiCallback) : ApiEvent(ApiEventType.CALLBACK) {

    constructor(enrolmentCallbackEvent: EnrolmentCallbackEvent) :
        this((enrolmentCallbackEvent.payload as EnrolmentCallbackPayload).creationTime,
            fromDomainToApiCallback(enrolmentCallbackEvent))

    constructor(identificationCallbackEvent: IdentificationCallbackEvent) :
        this((identificationCallbackEvent.payload as IdentificationCallbackPayload).creationTime,
            fromDomainToApiCallback(identificationCallbackEvent))

    constructor(verificationCallbackEvent: VerificationCallbackEvent) :
        this((verificationCallbackEvent.payload as VerificationCallbackPayload).creationTime,
            fromDomainToApiCallback(verificationCallbackEvent))

    constructor(refusalCallbackEvent: RefusalCallbackEvent) :
        this((refusalCallbackEvent.payload as RefusalCallbackPayload).creationTime,
            fromDomainToApiCallback(refusalCallbackEvent))

    constructor(confirmationCallbackEvent: ConfirmationCallbackEvent) :
        this((confirmationCallbackEvent.payload as ConfirmationCallbackPayload).creationTime,
            fromDomainToApiCallback(confirmationCallbackEvent))

    constructor(errorCallbackEvent: ErrorCallbackEvent) :
        this((errorCallbackEvent.payload as EnrolmentCallbackPayload).creationTime,
            fromDomainToApiCallback(errorCallbackEvent))
}

fun fromDomainToApiCallback(event: Event): ApiCallback =
    when (event.payload) {
        is EnrolmentCallbackPayload -> with(event.payload) { ApiEnrolmentCallback(guid) }
        is IdentificationCallbackPayload -> with(event.payload) { ApiIdentificationCallback(sessionId, scores.map { it.fromDomainToApi() }) }
        is VerificationCallbackPayload -> with(event.payload) { ApiVerificationCallback(score.fromDomainToApi()) }
        is RefusalCallbackPayload -> with(event.payload) { ApiRefusalCallback(reason, extra) }
        is ConfirmationCallbackPayload -> with(event.payload) { ApiConfirmationCallback(identificationOutcome) }
        is ErrorCallbackEventPayload -> with(event.payload) { ApiErrorCallback(reason.fromDomainToApi()) }
        else -> throw IllegalArgumentException("Invalid CallbackEvent")
    }
