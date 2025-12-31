package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove
import com.simprints.infra.eventsync.event.remote.ApiEnrolmentRecordPayloadType
import com.simprints.infra.eventsync.event.remote.ApiExternalCredential
import com.simprints.infra.eventsync.event.remote.fromApiToDomain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(ApiEnrolmentRecordPayloadType.ENROLMENT_RECORD_MOVE_KEY)
internal data class ApiEnrolmentRecordMovePayload(
    val enrolmentRecordCreation: ApiEnrolmentRecordCreationInMove,
    val enrolmentRecordDeletion: ApiEnrolmentRecordDeletionInMove,
    override val type: ApiEnrolmentRecordPayloadType = ApiEnrolmentRecordPayloadType.EnrolmentRecordMove,
) : ApiEnrolmentRecordEventPayload() {
    @Keep
    @Serializable
    data class ApiEnrolmentRecordDeletionInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
    )

    @Keep
    @Serializable
    data class ApiEnrolmentRecordCreationInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<ApiBiometricReference>?,
        val externalCredential: ApiExternalCredential?,
    )
}

internal fun ApiEnrolmentRecordMovePayload.fromApiToDomain() = EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload(
    with(enrolmentRecordCreation) {
        EnrolmentRecordCreationInMove(
            subjectId = subjectId,
            projectId = projectId,
            moduleId = moduleId.asTokenizableEncrypted(),
            attendantId = attendantId.asTokenizableEncrypted(),
            biometricReferences = biometricReferences?.map { it.fromApiToDomain() },
            externalCredential = externalCredential?.fromApiToDomain(subjectId),
        )
    },
    enrolmentRecordDeletion.let {
        EnrolmentRecordDeletionInMove(
            subjectId = it.subjectId,
            projectId = it.projectId,
            moduleId = it.moduleId.asTokenizableEncrypted(),
            attendantId = it.attendantId.asTokenizableEncrypted(),
        )
    },
)
