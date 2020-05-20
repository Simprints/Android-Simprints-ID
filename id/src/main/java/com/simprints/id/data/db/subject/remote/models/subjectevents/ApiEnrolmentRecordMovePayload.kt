package com.simprints.id.data.db.subject.remote.models.subjectevents

import androidx.annotation.Keep
import com.simprints.id.data.db.subject.domain.subjectevents.EnrolmentRecordMovePayload

@Keep
data class ApiEnrolmentRecordMovePayload(
    val enrolmentRecordCreation: ApiEnrolmentRecordCreationPayload,
    val enrolmentRecordDeletion: ApiEnrolmentRecordDeletionPayload
) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_MOVE) {

    constructor(payload: EnrolmentRecordMovePayload) :
        this(ApiEnrolmentRecordCreationPayload(payload.enrolmentRecordCreation ?: throw IllegalStateException("Domain creation payload should always have biometric references")),
            ApiEnrolmentRecordDeletionPayload(payload.enrolmentRecordDeletion))
}
