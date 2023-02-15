package com.simprints.eventsystem.event.remote.models.subject

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordMoveEvent

@Keep
class ApiEnrolmentRecordEvent(
    val id: String,
    val payload: ApiEnrolmentRecordEventPayload
)

fun ApiEnrolmentRecordEvent.fromApiToDomain(): EnrolmentRecordEvent =
    when (payload.type) {
        ApiEnrolmentRecordPayloadType.EnrolmentRecordCreation -> EnrolmentRecordCreationEvent(
            id,
            (payload as ApiEnrolmentRecordCreationPayload).fromApiToDomain()
        )
        ApiEnrolmentRecordPayloadType.EnrolmentRecordDeletion -> EnrolmentRecordDeletionEvent(
            id,
            (payload as ApiEnrolmentRecordDeletionPayload).fromApiToDomain()
        )
        ApiEnrolmentRecordPayloadType.EnrolmentRecordMove -> EnrolmentRecordMoveEvent(
            id,
            (payload as ApiEnrolmentRecordMovePayload).fromApiToDomain()
        )
    }
