package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
sealed class BiometricReference {
    abstract val id: String
    abstract val type: BiometricReferenceType
    abstract val format: String
}

@ExcludedFromGeneratedTestCoverageReports("Domain model")
@Serializable
@SerialName(BiometricReferenceType.FACE_REFERENCE_KEY)
data class FaceReference(
    override val id: String,
    val templates: List<FaceTemplate>,
    override val format: String,
    val metadata: Map<String, String>? = null,
) : BiometricReference() {
    override val type: BiometricReferenceType
        get() = BiometricReferenceType.FACE_REFERENCE
}

@ExcludedFromGeneratedTestCoverageReports("Domain model")
@Serializable
@SerialName(BiometricReferenceType.FINGERPRINT_REFERENCE_KEY)
data class FingerprintReference(
    override val id: String,
    val templates: List<FingerprintTemplate>,
    override val format: String,
    val metadata: Map<String, String>? = null,
) : BiometricReference() {
    override val type: BiometricReferenceType
        get() = BiometricReferenceType.FINGERPRINT_REFERENCE
}

@Serializable
enum class BiometricReferenceType {
    // a constant key is required to serialise/deserialize
    // Add a key in the companion object for each enum value

    // key added: FACE_REFERENCE
    FACE_REFERENCE,

    // key added: FINGERPRINT_REFERENCE
    FINGERPRINT_REFERENCE,

    ;

    companion object {
        const val FACE_REFERENCE_KEY = "FACE_REFERENCE"
        const val FINGERPRINT_REFERENCE_KEY = "FINGERPRINT_REFERENCE"
    }
}
