package com.simprints.infra.eventsync.event.remote.models.subject

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fromApiToDomain


@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiEnrolmentRecordMovePayload(
    val enrolmentRecordCreation: ApiEnrolmentRecordCreationInMove,
    val enrolmentRecordDeletion: ApiEnrolmentRecordDeletionInMove
) : ApiEnrolmentRecordEventPayload(ApiEnrolmentRecordPayloadType.EnrolmentRecordMove) {

    @Keep
    data class ApiEnrolmentRecordDeletionInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String
    )

    @Keep
    data class ApiEnrolmentRecordCreationInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<ApiBiometricReference>?
    )

    companion object {
        const val ENROLMENT_RECORD_MOVE = "EnrolmentRecordMove"
    }
}


internal fun ApiEnrolmentRecordMovePayload.fromApiToDomain() =
    EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload(
        with(enrolmentRecordCreation) {
            EnrolmentRecordCreationInMove(
                subjectId,
                projectId,
                moduleId,
                attendantId,
                biometricReferences?.map { it.fromApiToDomain() })
        },
        enrolmentRecordDeletion.let {
            EnrolmentRecordDeletionInMove(
                it.subjectId,
                it.projectId,
                it.moduleId,
                it.attendantId
            )
        }
    )
