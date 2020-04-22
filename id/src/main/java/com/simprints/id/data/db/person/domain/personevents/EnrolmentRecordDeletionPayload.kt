package com.simprints.id.data.db.person.domain.personevents

import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordDeletionPayload

data class EnrolmentRecordDeletionPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String
) : EventPayload(EventPayloadType.EnrolmentRecordDeletion) {

    constructor(payload: ApiEnrolmentRecordDeletionPayload) :
        this(payload.subjectId, payload.projectId, payload.moduleId, payload.attendantId)
}
