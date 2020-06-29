package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.callback.*

@Keep
class ApiCallbackEvent(id: String,
                       labels: List<Event.EventLabel>,
                       payload: EventPayload) :
    ApiEvent(id, labels.fromDomainToApi(), payload.fromDomainToApi()) {


    constructor(enrolmentCallbackEvent: EnrolmentCallbackEvent) :
        this(enrolmentCallbackEvent.id, enrolmentCallbackEvent.labels, enrolmentCallbackEvent.payload)

    constructor(identificationCallbackEvent: IdentificationCallbackEvent) :
        this(identificationCallbackEvent.id, identificationCallbackEvent.labels, identificationCallbackEvent.payload)

    constructor(verificationCallbackEvent: VerificationCallbackEvent) :
        this(verificationCallbackEvent.id, verificationCallbackEvent.labels, verificationCallbackEvent.payload)

    constructor(refusalCallbackEvent: RefusalCallbackEvent) :
        this(refusalCallbackEvent.id, refusalCallbackEvent.labels, refusalCallbackEvent.payload)

    constructor(confirmationCallbackEvent: ConfirmationCallbackEvent) :
        this(confirmationCallbackEvent.id, confirmationCallbackEvent.labels, confirmationCallbackEvent.payload)

    constructor(errorCallbackEvent: ErrorCallbackEvent) :
        this(errorCallbackEvent.id, errorCallbackEvent.labels, errorCallbackEvent.payload)

}
