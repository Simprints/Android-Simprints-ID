package com.simprints.id.data.db.event.domain.models

import com.beust.klaxon.TypeFor
import com.simprints.id.data.db.event.local.EventAdapter

//@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonTypeInfo(
//    use = JsonTypeInfo.Id.NAME,
//    include = JsonTypeInfo.As.EXISTING_PROPERTY,
//    property = "type",
//    visible = true)
//@JsonSubTypes(
//    JsonSubTypes.Type(value = ConfirmationCallbackPayload::class, name = "CALLBACK_CONFIRMATION"),
//    JsonSubTypes.Type(value = EnrolmentCallbackPayload::class, name = "CALLBACK_ENROLMENT"),
//    JsonSubTypes.Type(value = ErrorCallbackPayload::class, name = "CALLBACK_ERROR"),
//    JsonSubTypes.Type(value = IdentificationCallbackPayload::class, name = "CALLBACK_IDENTIFICATION"),
//    JsonSubTypes.Type(value = RefusalCallbackPayload::class, name = "CALLBACK_REFUSAL"),
//    JsonSubTypes.Type(value = VerificationCallbackPayload::class, name = "CALLBACK_VERIFICATION")
//)
@TypeFor(field = "type", adapter = EventAdapter::class)
abstract class EventPayload(
    open val type: EventType,
    open val eventVersion: Int,
    open val createdAt: Long,
    open var endedAt: Long = 0
)
