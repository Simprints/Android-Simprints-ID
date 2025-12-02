package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent

/**
 * V1 external schema for enrolment record events (polymorphic base type).
 *
 * Uses Jackson polymorphic serialization with "type" discriminator field.
 */
@Keep
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = CoSyncEnrolmentRecordCreationEventV1::class, name = "EnrolmentRecordCreation"),
)
sealed class CoSyncEnrolmentRecordEventV1(
    open val id: String,
    val type: String,
)

/**
 * Converts internal EnrolmentRecordEvent to V1 external schema.
 */
fun EnrolmentRecordEvent.toCoSyncV1(): CoSyncEnrolmentRecordEventV1 = when (this) {
    is EnrolmentRecordCreationEvent -> this.toCoSyncV1()
    else -> throw IllegalArgumentException("Unsupported event type for V1: ${this::class.simpleName}")
}

/**
 * Converts V1 external schema to internal EnrolmentRecordEvent.
 */
fun CoSyncEnrolmentRecordEventV1.toDomain(): EnrolmentRecordEvent = when (this) {
    is CoSyncEnrolmentRecordCreationEventV1 -> this.toDomain()
}
