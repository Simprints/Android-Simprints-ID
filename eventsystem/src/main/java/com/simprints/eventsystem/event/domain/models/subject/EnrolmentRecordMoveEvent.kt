package com.simprints.eventsystem.event.domain.models.subject

import androidx.annotation.Keep
import java.util.*

@Keep
data class EnrolmentRecordMoveEvent(
    override val id: String,
    val payload: EnrolmentRecordMovePayload,
) : EnrolmentRecordEvent(id, EnrolmentRecordEventType.EnrolmentRecordMove) {

    constructor(
        enrolmentRecordCreation: EnrolmentRecordCreationInMove,
        enrolmentRecordDeletion: EnrolmentRecordDeletionInMove,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentRecordMovePayload(enrolmentRecordCreation, enrolmentRecordDeletion)
    )

    data class EnrolmentRecordMovePayload(
        val enrolmentRecordCreation: EnrolmentRecordCreationInMove,
        val enrolmentRecordDeletion: EnrolmentRecordDeletionInMove,
    )

    data class EnrolmentRecordDeletionInMove(
        val subjectId: String, val projectId: String, val moduleId: String, val attendantId: String
    )

    data class EnrolmentRecordCreationInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<BiometricReference>?
    )
}
