package com.simprints.eventsystem.event.remote.models.subject

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordDeletionEvent

@Keep
data class ApiEnrolmentRecordDeletionPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String
) : ApiEnrolmentRecordEventPayload(ApiEnrolmentRecordPayloadType.EnrolmentRecordDeletion) {

    companion object {
        const val ENROLMENT_RECORD_DELETION = "EnrolmentRecordDeletion"
    }
}

fun ApiEnrolmentRecordDeletionPayload.fromApiToDomain() =
    EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload(
        subjectId,
        projectId,
        moduleId,
        attendantId
    )
