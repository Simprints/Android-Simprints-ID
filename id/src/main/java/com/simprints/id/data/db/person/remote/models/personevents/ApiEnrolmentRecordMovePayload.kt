package com.simprints.id.data.db.person.remote.models.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordMovePayload

@Keep
data class ApiEnrolmentRecordMovePayload(
    val enrolmentRecordCreationPayload: ApiEnrolmentRecordCreationPayload,
    val enrolmentRecordDeletionPayload: ApiEnrolmentRecordDeletionPayload
) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_MOVE) {

    constructor(payload: EnrolmentRecordMovePayload) :
        this(ApiEnrolmentRecordCreationPayload(payload.enrolmentRecordCreationPayload),
            ApiEnrolmentRecordDeletionPayload(payload.enrolmentRecordDeletionPayload))
}
