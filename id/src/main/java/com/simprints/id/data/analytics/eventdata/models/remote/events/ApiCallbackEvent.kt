package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.domain.events.Event
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.*
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiCallback
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiErrorCallback
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.fromDomainToApi
import java.lang.IllegalArgumentException

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
        is ErrorCallbackEvent -> with(event) { ApiErrorCallback(reason.fromDomainToApi()) }
        else -> throw IllegalArgumentException("Invalid CallbackEvent")
    }
