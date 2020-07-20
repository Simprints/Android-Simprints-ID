package com.simprints.id.data.db.event.domain.models.subject

import com.simprints.id.data.db.event.remote.events.subject.ApiBiometricReference
import com.simprints.id.data.db.event.remote.events.subject.ApiFaceReference
import com.simprints.id.data.db.event.remote.events.subject.ApiFingerprintReference

sealed class BiometricReference(val type: BiometricReferenceType)

data class FaceReference(val templates: List<FaceTemplate>,
                         val metadata: HashMap<String, String>? = null): BiometricReference(BiometricReferenceType.FACE_REFERENCE)

data class FingerprintReference(val templates: List<FingerprintTemplate>,
                                val metadata: HashMap<String, String>? = null): BiometricReference(BiometricReferenceType.FINGERPRINT_REFERENCE)

enum class BiometricReferenceType {
    FACE_REFERENCE,
    FINGERPRINT_REFERENCE
}

fun ApiBiometricReference.fromApiToDomain() = when(this) {
    is ApiFaceReference -> this.fromApiToDomain()
    is ApiFingerprintReference -> this.fromApiToDomain()
}

fun ApiFaceReference.fromApiToDomain() = FaceReference(templates.map { it.fromApiToDomain() }, metadata)

fun ApiFingerprintReference.fromApiToDomain() = FingerprintReference(templates.map { it.fromApiToDomain() }, metadata)
