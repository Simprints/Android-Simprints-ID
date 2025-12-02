package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent

/**
 * V1 external schema for enrolment record creation event.
 *
 * This represents the stable external contract for biometric enrolment data
 * sent to CommCare's subjectActions field.
 */
@Keep
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CoSyncEnrolmentRecordCreationEventV1(
    override val id: String,
    val payload: CoSyncEnrolmentRecordCreationPayloadV1,
) : CoSyncEnrolmentRecordEventV1(id, "EnrolmentRecordCreation")

/**
 * V1 payload for enrolment record creation event.
 *
 * Field names and types MUST remain stable for compatibility.
 */
@Keep
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CoSyncEnrolmentRecordCreationPayloadV1(
    /**
     * Unique identifier for the enrolled subject.
     */
    val subjectId: String,
    /**
     * Project identifier.
     */
    val projectId: String,
    /**
     * Module identifier. Can be plain string or TokenizableStringV1.
     * Supports both old format (plain string) and new format (TokenizableStringV1 object).
     */
    val moduleId: TokenizableStringV1,
    /**
     * Attendant identifier. Can be plain string or TokenizableStringV1.
     * Supports both old format (plain string) and new format (TokenizableStringV1 object).
     */
    val attendantId: TokenizableStringV1,
    /**
     * List of biometric references (face/fingerprint templates).
     */
    val biometricReferences: List<BiometricReferenceV1>,
    /**
     * Optional list of external credentials (e.g., MFID).
     * Empty list if not applicable.
     */
    val externalCredentials: List<ExternalCredentialV1>? = null,
)

/**
 * Converts internal EnrolmentRecordCreationEvent to V1 external schema.
 */
fun EnrolmentRecordCreationEvent.toCoSyncV1() = CoSyncEnrolmentRecordCreationEventV1(
    id = id,
    payload = payload.toCoSyncV1(),
)

/**
 * Converts V1 external schema to internal EnrolmentRecordCreationEvent.
 */
fun CoSyncEnrolmentRecordCreationEventV1.toDomain() = EnrolmentRecordCreationEvent(
    id = id,
    payload = payload.toDomain(),
)

/**
 * Converts internal payload to V1 external schema.
 */
fun EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload.toCoSyncV1() = CoSyncEnrolmentRecordCreationPayloadV1(
    subjectId = subjectId,
    projectId = projectId,
    moduleId = moduleId.toCoSyncV1(),
    attendantId = attendantId.toCoSyncV1(),
    biometricReferences = biometricReferences.map { it.toCoSyncV1() },
    externalCredentials = if (externalCredentials.isNotEmpty()) {
        externalCredentials.map { it.toCoSyncV1() }
    } else {
        null
    },
)

/**
 * Converts V1 external schema to internal payload.
 */
fun CoSyncEnrolmentRecordCreationPayloadV1.toDomain() = EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload(
    subjectId = subjectId,
    projectId = projectId,
    moduleId = moduleId.toDomain(),
    attendantId = attendantId.toDomain(),
    biometricReferences = biometricReferences.map { it.toDomain() },
    externalCredentials = externalCredentials?.map { it.toDomain() } ?: emptyList(),
)
