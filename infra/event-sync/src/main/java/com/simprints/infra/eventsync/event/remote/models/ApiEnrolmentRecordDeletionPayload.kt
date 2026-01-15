package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EnrolmentRecordDeletionEvent
import com.simprints.infra.eventsync.event.remote.ApiEnrolmentRecordPayloadType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(ApiEnrolmentRecordPayloadType.ENROLMENT_RECORD_DELETION_KEY)
internal data class ApiEnrolmentRecordDeletionPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
    override val type: ApiEnrolmentRecordPayloadType = ApiEnrolmentRecordPayloadType.EnrolmentRecordDeletion,
) : ApiEnrolmentRecordEventPayload()

internal fun ApiEnrolmentRecordDeletionPayload.fromApiToDomain() = EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload(
    subjectId,
    projectId,
    moduleId,
    attendantId,
)
