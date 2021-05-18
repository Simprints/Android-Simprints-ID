package com.simprints.eventsystem.event.remote.models.subject

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnore
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.eventsystem.event.remote.models.ApiEventPayload
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType

@Keep
data class ApiEnrolmentRecordDeletionPayload(
    @JsonIgnore override val startTime: Long = 0, //Not added on down-sync API yet
    override val version: Int,
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String
) : ApiEventPayload(ApiEventPayloadType.EnrolmentRecordDeletion, version, startTime) {

    constructor(payload: EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload) :
        this(payload.createdAt, payload.eventVersion, payload.subjectId, payload.projectId, payload.moduleId, payload.attendantId)
}

fun ApiEnrolmentRecordDeletionPayload.fromApiToDomain() =
    EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload(
        startTime,
        version,
        subjectId,
        projectId,
        moduleId,
        attendantId
    )
