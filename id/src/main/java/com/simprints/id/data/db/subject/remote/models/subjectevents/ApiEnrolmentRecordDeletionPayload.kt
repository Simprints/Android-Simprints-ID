package com.simprints.id.data.db.subject.remote.models.personevents

import com.simprints.id.data.db.subject.domain.personevents.EnrolmentRecordDeletionPayload
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
