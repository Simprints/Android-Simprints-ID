package com.simprints.id.data.db.event.remote.events.subject

import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType
import io.realm.internal.Keep

@Keep
data class ApiEnrolmentRecordDeletionPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String
) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_DELETION) {

    constructor(payload: EnrolmentRecordDeletionPayload) :
        this(payload.subjectId, payload.projectId, payload.moduleId, payload.attendantId)
}

fun ApiEnrolmentRecordDeletionPayload.fromApiToDomainDeletion() =
    EnrolmentRecordDeletionPayload(subjectId, projectId, moduleId, attendantId)
