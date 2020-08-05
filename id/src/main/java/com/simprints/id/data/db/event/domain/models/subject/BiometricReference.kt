package com.simprints.id.data.db.event.domain.models.subject

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.id.data.db.event.domain.models.callback.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.id.data.db.event.domain.models.subject.BiometricReferenceType.Companion.FACE_REFERENCE_KEY
import com.simprints.id.data.db.event.domain.models.subject.BiometricReferenceType.Companion.FINGERPRINT_REFERENCE_KEY
import com.simprints.id.data.db.event.remote.models.subject.ApiBiometricReference
import com.simprints.id.data.db.event.remote.models.subject.ApiBiometricReferenceType
import com.simprints.id.data.db.event.remote.models.subject.ApiFaceReference
import com.simprints.id.data.db.event.remote.models.subject.ApiFingerprintReference

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ConfirmationCallbackPayload::class, name = FACE_REFERENCE_KEY),
    JsonSubTypes.Type(value = EnrolmentCallbackPayload::class, name = FINGERPRINT_REFERENCE_KEY)
)
sealed class BiometricReference(val type: BiometricReferenceType)

data class FaceReference(val templates: List<FaceTemplate>,
                         val metadata: HashMap<String, String>? = null) : BiometricReference(BiometricReferenceType.FACE_REFERENCE)

data class FingerprintReference(val templates: List<FingerprintTemplate>,
                                val metadata: HashMap<String, String>? = null) : BiometricReference(BiometricReferenceType.FINGERPRINT_REFERENCE)

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

fun ApiFaceReference.fromApiToDomain() = FaceReference(templates.map { it.fromApiToDomain() }, metadata)

fun ApiFingerprintReference.fromApiToDomain() = FingerprintReference(templates.map { it.fromApiToDomain() }, metadata)
