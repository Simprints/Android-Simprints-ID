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
sealed class CoSyncBiometricReference {
    abstract val id: String
    abstract val format: String
}

@Keep
@Serializable
@SerialName("FACE_REFERENCE")
data class CoSyncFaceReference(
    override val id: String,
    val templates: List<CoSyncFaceTemplate>,
    override val format: String,
    val metadata: Map<String, String>? = null,
) : CoSyncBiometricReference()

@Keep
@Serializable
@SerialName("FINGERPRINT_REFERENCE")
data class CoSyncFingerprintReference(
    override val id: String,
    val templates: List<CoSyncFingerprintTemplate>,
    override val format: String,
    val metadata: Map<String, String>? = null,
) : CoSyncBiometricReference()

fun BiometricReference.toCoSync(): CoSyncBiometricReference = when (this) {
    is FaceReference -> toCoSync()
    is FingerprintReference -> toCoSync()
}

fun CoSyncBiometricReference.toEventDomain(): BiometricReference = when (this) {
    is CoSyncFaceReference -> toEventDomain()
    is CoSyncFingerprintReference -> toEventDomain()
}

fun FaceReference.toCoSync() = CoSyncFaceReference(
    id = id,
    templates = templates.map { it.toCoSync() },
    format = format,
    metadata = metadata,
)

fun CoSyncFaceReference.toEventDomain() = FaceReference(
    id = id,
    templates = templates.map { it.toEventDomain() },
    format = format,
    metadata = metadata,
)

fun FingerprintReference.toCoSync() = CoSyncFingerprintReference(
    id = id,
    templates = templates.map { it.toCoSync() },
    format = format,
    metadata = metadata,
)

fun CoSyncFingerprintReference.toEventDomain() = FingerprintReference(
    id = id,
    templates = templates.map { it.toEventDomain() },
    format = format,
    metadata = metadata,
)
