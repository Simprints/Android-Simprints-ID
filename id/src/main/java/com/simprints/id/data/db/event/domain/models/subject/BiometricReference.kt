package com.simprints.id.data.db.event.domain.models.subject

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.id.data.db.event.domain.models.subject.BiometricReferenceType.Companion.FACE_REFERENCE_KEY
import com.simprints.id.data.db.event.domain.models.subject.BiometricReferenceType.Companion.FINGERPRINT_REFERENCE_KEY
import com.simprints.id.data.db.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.id.data.db.event.remote.models.subject.biometricref.ApiBiometricReferenceType
import com.simprints.id.data.db.event.remote.models.subject.biometricref.face.ApiFaceReference
import com.simprints.id.data.db.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
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

enum class BiometricReferenceType {

    // a constant key is required to serialise/deserialize
    // BiometricReference correctly with Jackson (see annotation in BiometricReference).
    // Add a key in the companion object for each enum value

    /* key added: FACE_REFERENCE */
    FACE_REFERENCE,

    /* key added: FINGERPRINT_REFERENCE */
    FINGERPRINT_REFERENCE;

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
