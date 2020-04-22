package com.simprints.id.data.db.person.domain.personevents

import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordMovePayload

data class EnrolmentRecordMovePayload(
    val enrolmentRecordCreationPayload: EnrolmentRecordCreationPayload,
    val enrolmentRecordDeletionPayload: EnrolmentRecordDeletionPayload
) : EventPayload(EventPayloadType.EnrolmentRecordMove) {

    constructor(payload: ApiEnrolmentRecordMovePayload) :
        this(EnrolmentRecordCreationPayload(payload.enrolmentRecordCreationPayload),
            EnrolmentRecordDeletionPayload(payload.enrolmentRecordDeletionPayload))
}
