package com.simprints.id.data.db.subject.domain.subjectevents

import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEnrolmentRecordMovePayload

data class EnrolmentRecordMovePayload(
    val enrolmentRecordCreationPayload: EnrolmentRecordCreationPayload,
    val enrolmentRecordDeletionPayload: EnrolmentRecordDeletionPayload
) : EventPayload(EventPayloadType.ENROLMENT_RECORD_MOVE) {

    constructor(payload: ApiEnrolmentRecordMovePayload) :
        this(EnrolmentRecordCreationPayload(payload.enrolmentRecordCreationPayload),
            EnrolmentRecordDeletionPayload(payload.enrolmentRecordDeletionPayload))
}
