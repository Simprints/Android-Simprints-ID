package com.simprints.id.data.db.session.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.callback.*
import com.simprints.id.data.db.session.remote.events.callback.*

@Keep
class ApiCallbackEvent(val relativeStartTime: Long,
                       val callback: ApiCallback) : ApiEvent(ApiEventType.CALLBACK) {

    constructor(enrolmentCallbackEvent: EnrolmentCallbackEvent) :
        this(enrolmentCallbackEvent.relativeStartTime ?: 0,
            fromDomainToApiCallback(enrolmentCallbackEvent))

    constructor(identificationCallbackEvent: IdentificationCallbackEvent) :
        this(identificationCallbackEvent.relativeStartTime ?: 0,
            fromDomainToApiCallback(identificationCallbackEvent))

    constructor(verificationCallbackEvent: VerificationCallbackEvent) :
        this(verificationCallbackEvent.relativeStartTime ?: 0,
            fromDomainToApiCallback(verificationCallbackEvent))

    constructor(refusalCallbackEvent: RefusalCallbackEvent) :
        this(refusalCallbackEvent.relativeStartTime ?: 0,
            fromDomainToApiCallback(refusalCallbackEvent))

    constructor(confirmationCallbackEvent: ConfirmationCallbackEvent) :
        this(confirmationCallbackEvent.relativeStartTime ?: 0,
            fromDomainToApiCallback(confirmationCallbackEvent))

    constructor(errorCallbackEvent: ErrorCallbackEvent) :
        this(errorCallbackEvent.relativeStartTime ?: 0,
            fromDomainToApiCallback(errorCallbackEvent))
}

fun fromDomainToApiCallback(event: Event): ApiCallback =
    when (event) {
        is EnrolmentCallbackEvent -> with(event) { ApiEnrolmentCallback(guid) }
        is IdentificationCallbackEvent -> with(event) { ApiIdentificationCallback(sessionId, scores.map { it.fromDomainToApi() }) }
        is VerificationCallbackEvent -> with(event) { ApiVerificationCallback(score.fromDomainToApi()) }
        is RefusalCallbackEvent -> with(event) { ApiRefusalCallback(reason, extra) }
        is ConfirmationCallbackEvent -> with(event) { ApiConfirmationCallback(identificationOutcome) }
        is ErrorCallbackEvent -> with(event) { ApiErrorCallback(reason.fromDomainToApi()) }
        else -> throw IllegalArgumentException("Invalid CallbackEvent")
    }
