package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.BiometricReference
import com.simprints.infra.events.event.domain.models.FaceReference
import com.simprints.infra.events.event.domain.models.FingerprintReference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * V1 external schema for biometric references (polymorphic base type).
 * Stable external contract decoupled from internal [BiometricReference].
 */
@Keep
@Serializable
sealed class BiometricReferenceV1 {
    abstract val id: String
    abstract val format: String
}

@Keep
@Serializable
@SerialName("FACE_REFERENCE")
data class FaceReferenceV1(
    override val id: String,
    val templates: List<FaceTemplateV1>,
    override val format: String,
    val metadata: Map<String, String>? = null,
) : BiometricReferenceV1()

@Keep
@Serializable
@SerialName("FINGERPRINT_REFERENCE")
data class FingerprintReferenceV1(
    override val id: String,
    val templates: List<FingerprintTemplateV1>,
    override val format: String,
    val metadata: Map<String, String>? = null,
) : BiometricReferenceV1()

fun BiometricReference.toCoSyncV1(): BiometricReferenceV1 = when (this) {
    is FaceReference -> toCoSyncV1()
    is FingerprintReference -> toCoSyncV1()
}

fun BiometricReferenceV1.toDomain(): BiometricReference = when (this) {
    is FaceReferenceV1 -> toDomain()
    is FingerprintReferenceV1 -> toDomain()
}

fun FaceReference.toCoSyncV1() = FaceReferenceV1(
    id = id,
    templates = templates.map { it.toCoSyncV1() },
    format = format,
    metadata = metadata,
)

fun FaceReferenceV1.toDomain() = FaceReference(
    id = id,
    templates = templates.map { it.toDomain() },
    format = format,
    metadata = metadata,
)

fun FingerprintReference.toCoSyncV1() = FingerprintReferenceV1(
    id = id,
    templates = templates.map { it.toCoSyncV1() },
    format = format,
    metadata = metadata,
)

fun FingerprintReferenceV1.toDomain() = FingerprintReference(
    id = id,
    templates = templates.map { it.toDomain() },
    format = format,
    metadata = metadata,
)
