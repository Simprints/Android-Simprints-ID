package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.infra.events.event.domain.models.subject.BiometricReference
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference

/**
 * V1 external schema for biometric references (polymorphic base type).
 *
 * Uses Jackson polymorphic serialization with "type" discriminator field.
 */
@Keep
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = FaceReferenceV1::class, name = "FACE_REFERENCE"),
    JsonSubTypes.Type(value = FingerprintReferenceV1::class, name = "FINGERPRINT_REFERENCE"),
)
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class BiometricReferenceV1(
    open val id: String,
    open val format: String,
    val type: String,
)

/**
 * V1 face reference with face templates.
 */
@Keep
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FaceReferenceV1(
    override val id: String,
    val templates: List<FaceTemplateV1>,
    override val format: String,
    val metadata: Map<String, String>? = null,
) : BiometricReferenceV1(id, format, "FACE_REFERENCE")

/**
 * V1 fingerprint reference with fingerprint templates.
 */
@Keep
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FingerprintReferenceV1(
    override val id: String,
    val templates: List<FingerprintTemplateV1>,
    override val format: String,
    val metadata: Map<String, String>? = null,
) : BiometricReferenceV1(id, format, "FINGERPRINT_REFERENCE")

/**
 * Converts internal BiometricReference to V1 external schema.
 */
fun BiometricReference.toCoSyncV1(): BiometricReferenceV1 = when (this) {
    is FaceReference -> this.toCoSyncV1()
    is FingerprintReference -> this.toCoSyncV1()
}

/**
 * Converts V1 external schema to internal BiometricReference.
 */
fun BiometricReferenceV1.toDomain(): BiometricReference = when (this) {
    is FaceReferenceV1 -> this.toDomain()
    is FingerprintReferenceV1 -> this.toDomain()
}

/**
 * Converts internal FaceReference to V1 external schema.
 */
fun FaceReference.toCoSyncV1() = FaceReferenceV1(
    id = id,
    templates = templates.map { it.toCoSyncV1() },
    format = format,
    metadata = metadata,
)

/**
 * Converts V1 external schema to internal FaceReference.
 */
fun FaceReferenceV1.toDomain() = FaceReference(
    id = id,
    templates = templates.map { it.toDomain() },
    format = format,
    metadata = metadata,
)

/**
 * Converts internal FingerprintReference to V1 external schema.
 */
fun FingerprintReference.toCoSyncV1() = FingerprintReferenceV1(
    id = id,
    templates = templates.map { it.toCoSyncV1() },
    format = format,
    metadata = metadata,
)

/**
 * Converts V1 external schema to internal FingerprintReference.
 */
fun FingerprintReferenceV1.toDomain() = FingerprintReference(
    id = id,
    templates = templates.map { it.toDomain() },
    format = format,
    metadata = metadata,
)
