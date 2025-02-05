package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = EnrolmentRecordCreationEvent::class, name = "EnrolmentRecordCreation"),
    JsonSubTypes.Type(value = EnrolmentRecordMoveEvent::class, name = "EnrolmentRecordMove"),
    JsonSubTypes.Type(value = EnrolmentRecordDeletionEvent::class, name = "EnrolmentRecordDeletion"),
    JsonSubTypes.Type(value = EnrolmentRecordUpdateEvent::class, name = "EnrolmentRecordUpdate"),
)
@Keep
sealed class EnrolmentRecordEvent(
    open val id: String,
    val type: EnrolmentRecordEventType,
)
