package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import kotlinx.serialization.Serializable

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
@Serializable
sealed class EnrolmentRecordEvent {
    abstract val id: String
    abstract val type: EnrolmentRecordEventType
}
