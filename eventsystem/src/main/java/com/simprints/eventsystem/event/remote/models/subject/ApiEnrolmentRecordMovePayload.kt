package com.simprints.eventsystem.event.remote.models.subject

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove
import com.simprints.eventsystem.event.domain.models.subject.fromApiToDomain
import com.simprints.eventsystem.event.remote.models.subject.biometricref.ApiBiometricReference


@Keep
@JsonInclude(Include.NON_NULL)
data class ApiEnrolmentRecordMovePayload(
    val enrolmentRecordCreation: ApiEnrolmentRecordCreationInMove,
    val enrolmentRecordDeletion: ApiEnrolmentRecordDeletionInMove
) : ApiEnrolmentRecordEventPayload(ApiEnrolmentRecordPayloadType.EnrolmentRecordMove) {

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

    companion object {
        const val ENROLMENT_RECORD_MOVE = "EnrolmentRecordMove"
    }
}


fun ApiEnrolmentRecordMovePayload.fromApiToDomain() =
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
