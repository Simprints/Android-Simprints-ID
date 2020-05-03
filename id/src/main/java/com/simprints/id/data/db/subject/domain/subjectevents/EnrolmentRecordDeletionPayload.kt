package com.simprints.id.data.db.subject.domain.personevents

import com.simprints.id.data.db.subject.remote.models.personevents.ApiEnrolmentRecordDeletionPayload

data class EnrolmentRecordDeletionPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: String,
    val attendantId: String
) : EventPayload(EventPayloadType.ENROLMENT_RECORD_DELETION) {

    constructor(payload: ApiEnrolmentRecordDeletionPayload) :
        this(payload.subjectId, payload.projectId, payload.moduleId, payload.attendantId)
}
