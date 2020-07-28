package com.simprints.id.data.db.event.remote.models.subject

import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType
import io.realm.internal.Keep

@Keep
class ApiEnrolmentRecordDeletionPayload(
    createdAt: Long,
    version: Int,
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String
) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_DELETION, version, createdAt) {

    constructor(payload: EnrolmentRecordDeletionPayload) :
        this(payload.createdAt, payload.eventVersion, payload.subjectId, payload.projectId, payload.moduleId, payload.attendantId)
}


fun ApiEnrolmentRecordDeletionPayload.fromApiToDomain() =
    EnrolmentRecordDeletionPayload(
        createdAt ?: 0,
        version,
        subjectId,
        projectId,
        moduleId,
        attendantId
    )
