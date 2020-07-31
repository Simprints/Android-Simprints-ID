package com.simprints.id.data.db.event.remote.models.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType
import com.simprints.id.data.db.event.remote.models.fromDomainToApi


@Keep
class ApiEnrolmentRecordMovePayload(
    createdAt: Long,
    version: Int,
    val enrolmentRecordCreation: ApiEnrolmentRecordCreationPayload?,
    val enrolmentRecordDeletion: ApiEnrolmentRecordDeletionPayload
) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_MOVE, version, createdAt) {

    constructor(payload: EnrolmentRecordMovePayload) : this(
        payload.createdAt,
        payload.eventVersion,
        payload.enrolmentRecordCreation?.fromDomainToApi() as ApiEnrolmentRecordCreationPayload?,
        payload.enrolmentRecordDeletion.fromDomainToApi() as ApiEnrolmentRecordDeletionPayload)
}


fun ApiEnrolmentRecordMovePayload.fromApiToDomain() =
    EnrolmentRecordMovePayload(
        relativeStartTime ?: 0,
        version,
        enrolmentRecordCreation?.fromApiToDomain(),
        enrolmentRecordDeletion.fromApiToDomain()
    )
