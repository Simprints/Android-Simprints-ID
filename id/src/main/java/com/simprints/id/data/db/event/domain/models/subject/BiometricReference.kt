package com.simprints.id.data.db.event.domain.models.subject

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.id.data.db.event.domain.models.subject.BiometricReferenceType.Companion.FACE_REFERENCE_KEY
import com.simprints.id.data.db.event.domain.models.subject.BiometricReferenceType.Companion.FINGERPRINT_REFERENCE_KEY
import com.simprints.id.data.db.event.remote.models.subject.ApiBiometricReference
import com.simprints.id.data.db.event.remote.models.subject.ApiBiometricReferenceType
import com.simprints.id.data.db.event.remote.models.subject.ApiFaceReference
import com.simprints.id.data.db.event.remote.models.subject.ApiFingerprintReference

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = FaceReference::class, name = FACE_REFERENCE_KEY),
    JsonSubTypes.Type(value = FingerprintReference::class, name = FINGERPRINT_REFERENCE_KEY)
)
sealed class BiometricReference(open val id: String,
                                val type: BiometricReferenceType)

data class FaceReference(override val id: String,
                         val templates: List<FaceTemplate>,
                         val metadata: HashMap<String, String>? = null) : BiometricReference(id, BiometricReferenceType.FACE_REFERENCE)

data class FingerprintReference(override val id: String,
                                val templates: List<FingerprintTemplate>,
                                val metadata: HashMap<String, String>? = null) : BiometricReference(id, BiometricReferenceType.FINGERPRINT_REFERENCE)

enum class BiometricReferenceType(private val key: String) {
    FACE_REFERENCE(BiometricReferenceType.FACE_REFERENCE_KEY),
    FINGERPRINT_REFERENCE(BiometricReferenceType.FINGERPRINT_REFERENCE_KEY);

    companion object {
        const val FACE_REFERENCE_KEY = "FACE_REFERENCE"
        const val FINGERPRINT_REFERENCE_KEY = "FINGERPRINT_REFERENCE"
    }
}

fun ApiBiometricReference.fromApiToDomain() = when (this.type) {
    ApiBiometricReferenceType.FaceReference -> (this as ApiFaceReference).fromApiToDomain()
    ApiBiometricReferenceType.FingerprintReference -> (this as ApiFingerprintReference).fromApiToDomain()
}

fun ApiFaceReference.fromApiToDomain() = FaceReference(id, templates.map { it.fromApiToDomain() }, metadata)

fun ApiFingerprintReference.fromApiToDomain() = FingerprintReference(id, templates.map { it.fromApiToDomain() }, metadata)
