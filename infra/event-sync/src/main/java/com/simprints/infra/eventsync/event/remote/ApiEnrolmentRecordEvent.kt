package com.simprints.infra.eventsync.event.remote

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.EnrolmentRecordDeletionEvent
import com.simprints.infra.events.event.domain.models.EnrolmentRecordEvent
import com.simprints.infra.events.event.domain.models.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.EnrolmentRecordUpdateEvent
import com.simprints.infra.eventsync.event.remote.models.ApiEnrolmentRecordCreationPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEnrolmentRecordDeletionPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEnrolmentRecordEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEnrolmentRecordMovePayload
import com.simprints.infra.eventsync.event.remote.models.ApiEnrolmentRecordUpdatePayload
import com.simprints.infra.eventsync.event.remote.models.fromApiToDomain
import kotlinx.serialization.Serializable

@Keep
@Serializable
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
