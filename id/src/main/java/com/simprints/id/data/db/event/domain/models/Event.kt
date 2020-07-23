package com.simprints.id.data.db.event.domain.models

import com.beust.klaxon.TypeFor
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.local.EventAdapter

//@JsonTypeInfo(
//    use = JsonTypeInfo.Id.NAME,
//    include = JsonTypeInfo.As.EXISTING_PROPERTY,
//    property = "type")
//@JsonSubTypes(
//    JsonSubTypes.Type(value = ConfirmationCallbackEvent::class, name = "CALLBACK_CONFIRMATION"),
//    JsonSubTypes.Type(value = EnrolmentCallbackEvent::class, name = "CALLBACK_ENROLMENT"),
//    JsonSubTypes.Type(value = ErrorCallbackEvent::class, name = "CALLBACK_ERROR"),
//    JsonSubTypes.Type(value = IdentificationCallbackEvent::class, name = "CALLBACK_IDENTIFICATION"),
//    JsonSubTypes.Type(value = RefusalCallbackEvent::class, name = "CALLBACK_REFUSAL"),
//    JsonSubTypes.Type(value = VerificationCallbackEvent::class, name = "CALLBACK_VERIFICATION")
//)
@TypeFor(field = "type", adapter = EventAdapter::class)
abstract class Event(open val id: String,
                     open val labels: MutableList<EventLabel>,
                     open val payload: EventPayload,
                     open val type: EventType = SESSION_CAPTURE) {

    companion object {
        const val DEFAULT_EVENT_VERSION = 0
    }
}


fun Event.getSessionLabelIfExists(): SessionIdLabel? =
    labels.firstOrNull { it is SessionIdLabel } as SessionIdLabel?
