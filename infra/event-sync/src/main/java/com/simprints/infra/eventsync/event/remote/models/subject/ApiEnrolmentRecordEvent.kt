package com.simprints.infra.eventsync.event.remote.models.subject

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent

@Keep
internal class ApiEnrolmentRecordEvent(
    val id: String,
    val payload: ApiEnrolmentRecordEventPayload,
)

internal fun ApiEnrolmentRecordEvent.fromApiToDomain(): EnrolmentRecordEvent = when (payload.type) {
    ApiEnrolmentRecordPayloadType.EnrolmentRecordCreation -> EnrolmentRecordCreationEvent(
        id,
        (payload as ApiEnrolmentRecordCreationPayload).fromApiToDomain(),
    )

    ApiEnrolmentRecordPayloadType.EnrolmentRecordDeletion -> EnrolmentRecordDeletionEvent(
        id,
        (payload as ApiEnrolmentRecordDeletionPayload).fromApiToDomain(),
    )

    ApiEnrolmentRecordPayloadType.EnrolmentRecordUpdate -> EnrolmentRecordUpdateEvent(
        id,
        (payload as ApiEnrolmentRecordUpdatePayload).fromApiToDomain(),
    )

    ApiEnrolmentRecordPayloadType.EnrolmentRecordMove -> EnrolmentRecordMoveEvent(
        id,
        (payload as ApiEnrolmentRecordMovePayload).fromApiToDomain(),
    )
}
