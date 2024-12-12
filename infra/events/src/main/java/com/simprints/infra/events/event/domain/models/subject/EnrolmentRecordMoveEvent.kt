package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import java.util.UUID

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
        EnrolmentRecordMovePayload(enrolmentRecordCreation, enrolmentRecordDeletion),
    )

    data class EnrolmentRecordMovePayload(
        val enrolmentRecordCreation: EnrolmentRecordCreationInMove,
        val enrolmentRecordDeletion: EnrolmentRecordDeletionInMove,
    )

    data class EnrolmentRecordDeletionInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: TokenizableString,
        val attendantId: TokenizableString,
    )

    data class EnrolmentRecordCreationInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: TokenizableString,
        val attendantId: TokenizableString,
        val biometricReferences: List<BiometricReference>?,
    )
}
