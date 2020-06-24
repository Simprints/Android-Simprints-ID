package com.simprints.id.data.db.event.remote.events.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType

@Keep
data class ApiEnrolmentRecordMovePayload(
    val enrolmentRecordCreation: ApiEnrolmentRecordCreationPayload,
    val enrolmentRecordDeletion: ApiEnrolmentRecordDeletionPayload
) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_MOVE) {

    constructor(payload: EnrolmentRecordMovePayload) :
        this(ApiEnrolmentRecordCreationPayload(payload.enrolmentRecordCreation
            ?: throw IllegalStateException("Domain creation payload should always have biometric references")),
            ApiEnrolmentRecordDeletionPayload(payload.enrolmentRecordDeletion))
}
