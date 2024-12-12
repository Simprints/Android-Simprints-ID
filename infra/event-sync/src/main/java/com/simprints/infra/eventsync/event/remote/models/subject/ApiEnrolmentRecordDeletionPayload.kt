package com.simprints.infra.eventsync.event.remote.models.subject

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordDeletionEvent

@Keep
internal data class ApiEnrolmentRecordDeletionPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String,
) : ApiEnrolmentRecordEventPayload(ApiEnrolmentRecordPayloadType.EnrolmentRecordDeletion) {
    companion object {
        const val ENROLMENT_RECORD_DELETION = "EnrolmentRecordDeletion"
    }
}

internal fun ApiEnrolmentRecordDeletionPayload.fromApiToDomain() = EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload(
    subjectId,
    projectId,
    moduleId,
    attendantId,
)
