package com.simprints.eventsystem.event.remote.models.subject.biometricref

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.eventsystem.event.domain.models.subject.BiometricReference
import com.simprints.eventsystem.event.domain.models.subject.FaceTemplate
import com.simprints.eventsystem.event.domain.models.subject.FingerprintTemplate
import com.simprints.eventsystem.event.remote.models.subject.biometricref.face.ApiFaceReference
import com.simprints.eventsystem.event.remote.models.subject.biometricref.face.ApiFaceTemplate
import com.simprints.eventsystem.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.eventsystem.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import com.simprints.eventsystem.event.domain.models.subject.FaceReference as DomainFaceReference
import com.simprints.eventsystem.event.domain.models.subject.FingerprintReference as DomainFingerprintReference

private const val FACE_REFERENCE_KEY = "FaceReference"
private const val FINGERPRINT_REFERENCE_KEY = "FingerprintReference"

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = ApiFaceReference::class, name = FACE_REFERENCE_KEY),
    JsonSubTypes.Type(value = ApiFingerprintReference::class, name = FINGERPRINT_REFERENCE_KEY)
)
interface ApiBiometricReference {
    val type: ApiBiometricReferenceType
    val id: String
}

@Keep
enum class ApiBiometricReferenceType {
    // a constant key is required to serialise/deserialize
    // ApiBiometricReference correctly with Jackson (see annotation in ApiBiometricReference).
    // Add a key in the companion object for each enum value

    /* key added: FACE_REFERENCE_KEY */
    FaceReference,

    /* key added: FINGERPRINT_REFERENCE_KEY */
    FingerprintReference;
}

fun BiometricReference.fromDomainToApi() = when (this) {
    is DomainFaceReference -> {
        ApiFaceReference(id, templates.map { it.fromDomainToApi() }, format, metadata)
    }
    is DomainFingerprintReference -> {
        ApiFingerprintReference(id, templates.map { it.fromDomainToApi() }, format, metadata)
    }
}

fun FaceTemplate.fromDomainToApi() = ApiFaceTemplate(template)

fun FingerprintTemplate.fromDomainToApi() =
    ApiFingerprintTemplate(quality, template, finger)





