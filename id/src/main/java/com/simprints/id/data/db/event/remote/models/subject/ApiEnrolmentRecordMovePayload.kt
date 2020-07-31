package com.simprints.id.data.db.event.remote.models.subject

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnore
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.*
import com.simprints.id.data.db.event.domain.models.subject.fromApiToDomain
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType


@Keep
class ApiEnrolmentRecordMovePayload(
    @JsonIgnore override val relativeStartTime: Long, //Not added on API yet
    override val version: Int,
    val enrolmentRecordCreation: ApiEnrolmentRecordCreationInMove?,
    val enrolmentRecordDeletion: ApiEnrolmentRecordDeletionInMove
) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_MOVE, version, relativeStartTime) {

    data class ApiEnrolmentRecordDeletionInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String
    )

    data class ApiEnrolmentRecordCreationInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<ApiBiometricReference>?
    )

    constructor(payload: EnrolmentRecordMovePayload) : this(
        payload.createdAt,
        payload.eventVersion,
        payload.enrolmentRecordCreation?.let {
            ApiEnrolmentRecordCreationInMove(it.subjectId, it.projectId, it.moduleId, it.attendantId, it.biometricReferences?.map { it.fromDomainToApi() })
        },
        payload.enrolmentRecordDeletion.let {
            ApiEnrolmentRecordDeletionInMove(it.subjectId, it.projectId, it.moduleId, it.attendantId)
        })
}


fun ApiEnrolmentRecordMovePayload.fromApiToDomain() =
    EnrolmentRecordMovePayload(
        relativeStartTime,
        version,
        enrolmentRecordCreation?.let {
            EnrolmentRecordCreationInMove(it.subjectId, it.projectId, it.moduleId, it.attendantId, it.biometricReferences?.map { it.fromApiToDomain() })
        },
        enrolmentRecordDeletion.let {
            EnrolmentRecordDeletionInMove(it.subjectId, it.projectId, it.moduleId, it.attendantId)
        }
    )
