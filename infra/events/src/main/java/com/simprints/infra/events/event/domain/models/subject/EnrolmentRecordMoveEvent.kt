package com.simprints.infra.events.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName("EnrolmentRecordMove")
data class EnrolmentRecordMoveEvent(
    override val id: String,
    val payload: EnrolmentRecordMovePayload,
) : EnrolmentRecordEvent() {
    override val type: EnrolmentRecordEventType
        get() = EnrolmentRecordEventType.EnrolmentRecordMove
    constructor(
        enrolmentRecordCreation: EnrolmentRecordCreationInMove,
        enrolmentRecordDeletion: EnrolmentRecordDeletionInMove,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentRecordMovePayload(enrolmentRecordCreation, enrolmentRecordDeletion),
    )

    @Keep
    @Serializable
    data class EnrolmentRecordMovePayload(
        val enrolmentRecordCreation: EnrolmentRecordCreationInMove,
        val enrolmentRecordDeletion: EnrolmentRecordDeletionInMove,
    )

    @Keep
    @Serializable
    data class EnrolmentRecordDeletionInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: TokenizableString,
        val attendantId: TokenizableString,
    )

    @Keep
    @Serializable
    data class EnrolmentRecordCreationInMove(
        val subjectId: String,
        val projectId: String,
        val moduleId: TokenizableString,
        val attendantId: TokenizableString,
        val biometricReferences: List<BiometricReference>?,
        val externalCredential: ExternalCredential?,
    )
}
