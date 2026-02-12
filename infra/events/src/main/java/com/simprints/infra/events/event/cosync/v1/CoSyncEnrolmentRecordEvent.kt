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
sealed class CoSyncEnrolmentRecordEvent {
    abstract val id: String
}

/**
 * V1 external schema for enrolment record creation event.
 */
@Keep
@Serializable
@SerialName("EnrolmentRecordCreation")
data class CoSyncEnrolmentRecordCreationEvent(
    override val id: String,
    val payload: CoSyncEnrolmentRecordCreationPayload,
) : CoSyncEnrolmentRecordEvent()

@Keep
@Serializable
data class CoSyncEnrolmentRecordCreationPayload(
    val subjectId: String,
    val projectId: String,
    val moduleId: CoSyncTokenizableString,
    val attendantId: CoSyncTokenizableString,
    val biometricReferences: List<CoSyncBiometricReference> = emptyList(),
    val externalCredentials: List<CoSyncExternalCredential> = emptyList(),
)

fun EnrolmentRecordEvent.toCoSync(): CoSyncEnrolmentRecordEvent = when (this) {
    is EnrolmentRecordCreationEvent -> toCoSync()
    else -> throw IllegalArgumentException("Unsupported event type for V1: ${this::class.simpleName}")
}

fun CoSyncEnrolmentRecordEvent.toEventDomain(): EnrolmentRecordEvent = when (this) {
    is CoSyncEnrolmentRecordCreationEvent -> toEventDomain()
}

fun EnrolmentRecordCreationEvent.toCoSync() = CoSyncEnrolmentRecordCreationEvent(
    id = id,
    payload = CoSyncEnrolmentRecordCreationPayload(
        subjectId = payload.subjectId,
        projectId = payload.projectId,
        moduleId = payload.moduleId.toCoSync(),
        attendantId = payload.attendantId.toCoSync(),
        biometricReferences = payload.biometricReferences.map { it.toCoSync() },
        externalCredentials = payload.externalCredentials.map { it.toCoSync() },
    ),
)

fun CoSyncEnrolmentRecordCreationEvent.toEventDomain() = EnrolmentRecordCreationEvent(
    id = id,
    payload = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
        subjectId = payload.subjectId,
        projectId = payload.projectId,
        moduleId = payload.moduleId.toDomain(),
        attendantId = payload.attendantId.toDomain(),
        biometricReferences = payload.biometricReferences.map { it.toEventDomain() },
        externalCredentials = payload.externalCredentials.map { it.toDomain() },
    ),
)
