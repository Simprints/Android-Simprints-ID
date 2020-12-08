package com.simprints.id.data.db.event.remote.models.subject

import com.fasterxml.jackson.annotation.JsonIgnore
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType
import io.realm.internal.Keep

@Keep
data class ApiEnrolmentRecordDeletionPayload(
    @JsonIgnore override val startTime: Long = 0, //Not added on down-sync API yet
    override val version: Int,
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String
) : ApiEventPayload(ApiEventPayloadType.EnrolmentRecordDeletion, version, startTime) {

    constructor(payload: EnrolmentRecordDeletionPayload) :
        this(payload.createdAt, payload.eventVersion, payload.subjectId, payload.projectId, payload.moduleId, payload.attendantId)
}

fun ApiEnrolmentRecordDeletionPayload.fromApiToDomain() =
    EnrolmentRecordDeletionPayload(
        startTime,
        version,
        subjectId,
        projectId,
        moduleId,
        attendantId
    )
