package com.simprints.id.data.db.person.domain.personevents

import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordMovePayload

data class EnrolmentRecordMovePayload(
    val enrolmentRecordCreation: EnrolmentRecordCreationPayload,
    val enrolmentRecordDeletion: EnrolmentRecordDeletionPayload
) : EventPayload(EventPayloadType.ENROLMENT_RECORD_MOVE) {

    constructor(payload: ApiEnrolmentRecordMovePayload) :
        this(EnrolmentRecordCreationPayload(payload.enrolmentRecordCreation),
            EnrolmentRecordDeletionPayload(payload.enrolmentRecordDeletion))
}
