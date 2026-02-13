package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.EnrolmentRecordEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * V1 external schema for enrolment record events (polymorphic base type).
 * Stable external contract decoupled from internal [EnrolmentRecordEvent].
 */
@Keep
@Serializable
sealed class EnrolmentRecordEventV1 {
    abstract val id: String
}

/**
 * V1 external schema for enrolment record creation event.
 */
@Keep
@Serializable
@SerialName("EnrolmentRecordCreation")
data class EnrolmentRecordCreationEventV1(
    override val id: String,
    val payload: EnrolmentRecordCreationPayloadV1,
) : EnrolmentRecordEventV1()

@Keep
@Serializable
data class EnrolmentRecordCreationPayloadV1(
    val subjectId: String,
    val projectId: String,
    val moduleId: TokenizableStringV1,
    val attendantId: TokenizableStringV1,
    val biometricReferences: List<BiometricReferenceV1> = emptyList(),
    val externalCredentials: List<ExternalCredentialV1> = emptyList(),
)

fun EnrolmentRecordEvent.toCoSyncV1(): EnrolmentRecordEventV1 = when (this) {
    is EnrolmentRecordCreationEvent -> toCoSyncV1()
    else -> throw IllegalArgumentException("Unsupported event type for V1: ${this::class.simpleName}")
}

fun EnrolmentRecordEventV1.toDomain(): EnrolmentRecordEvent = when (this) {
    is EnrolmentRecordCreationEventV1 -> toDomain()
}

fun EnrolmentRecordCreationEvent.toCoSyncV1() = EnrolmentRecordCreationEventV1(
    id = id,
    payload = EnrolmentRecordCreationPayloadV1(
        subjectId = payload.subjectId,
        projectId = payload.projectId,
        moduleId = payload.moduleId.toCoSyncV1(),
        attendantId = payload.attendantId.toCoSyncV1(),
        biometricReferences = payload.biometricReferences.map { it.toCoSyncV1() },
        externalCredentials = payload.externalCredentials.map { it.toCoSyncV1() },
    ),
)

fun EnrolmentRecordCreationEventV1.toDomain() = EnrolmentRecordCreationEvent(
    id = id,
    payload = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
        subjectId = payload.subjectId,
        projectId = payload.projectId,
        moduleId = payload.moduleId.toDomain(),
        attendantId = payload.attendantId.toDomain(),
        biometricReferences = payload.biometricReferences.map { it.toDomain() },
        externalCredentials = payload.externalCredentials.map { it.toDomain() },
    ),
)
