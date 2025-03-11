package com.simprints.infra.eventsync.event.remote.models.subject.biometricref

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.events.event.domain.models.subject.BiometricReference
import com.simprints.infra.events.event.domain.models.subject.EarTemplate
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ear.ApiEarReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.ear.ApiEarTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.face.ApiFaceReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.face.ApiFaceTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import com.simprints.infra.events.event.domain.models.subject.EarReference as DomainEarReference
import com.simprints.infra.events.event.domain.models.subject.FaceReference as DomainFaceReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference as DomainFingerprintReference

private const val FACE_REFERENCE_KEY = "FaceReference"
private const val FINGERPRINT_REFERENCE_KEY = "FingerprintReference"
private const val EAR_REFERENCE_KEY = "EarReference"

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = ApiFaceReference::class, name = FACE_REFERENCE_KEY),
    JsonSubTypes.Type(value = ApiFingerprintReference::class, name = FINGERPRINT_REFERENCE_KEY),
    JsonSubTypes.Type(value = ApiEarReference::class, name = EAR_REFERENCE_KEY),
)
internal interface ApiBiometricReference {
    val type: ApiBiometricReferenceType
    val id: String
}

@Keep
internal enum class ApiBiometricReferenceType {
    // a constant key is required to serialise/deserialize
    // ApiBiometricReference correctly with Jackson (see annotation in ApiBiometricReference).
    // Add a key in the companion object for each enum value

    // key added: FACE_REFERENCE_KEY
    FaceReference,

    // key added: FINGERPRINT_REFERENCE_KEY
    FingerprintReference,

    // key added: EAR_REFERENCE_KEY
    EarReference,
}

internal fun BiometricReference.fromDomainToApi() = when (this) {
    is DomainFaceReference -> {
        ApiFaceReference(id, templates.map { it.fromDomainToApi() }, format, metadata)
    }

    is DomainEarReference -> {
        ApiEarReference(id, templates.map { it.fromDomainToApi() }, format, metadata)
    }

    is DomainFingerprintReference -> {
        ApiFingerprintReference(id, templates.map { it.fromDomainToApi() }, format, metadata)
    }
}

internal fun ApiBiometricReference.fromApiToDomain() = when (this.type) {
    ApiBiometricReferenceType.FaceReference -> (this as ApiFaceReference).fromApiToDomain()
    ApiBiometricReferenceType.FingerprintReference -> (this as ApiFingerprintReference).fromApiToDomain()
    ApiBiometricReferenceType.EarReference -> (this as ApiEarReference).fromApiToDomain()
}

internal fun ApiFaceReference.fromApiToDomain() = DomainFaceReference(id, templates.map { it.fromApiToDomain() }, format, metadata)

internal fun ApiFaceTemplate.fromApiToDomain() = FaceTemplate(template)

internal fun FaceTemplate.fromDomainToApi() = ApiFaceTemplate(template)

internal fun ApiEarReference.fromApiToDomain() = DomainEarReference(id, templates.map { it.fromApiToDomain() }, format, metadata)

internal fun ApiEarTemplate.fromApiToDomain() = EarTemplate(template)

internal fun EarTemplate.fromDomainToApi() = ApiEarTemplate(template)

internal fun ApiFingerprintReference.fromApiToDomain() =
    DomainFingerprintReference(id, templates.map { it.fromApiToDomain() }, format, metadata)

internal fun ApiFingerprintTemplate.fromApiToDomain() = FingerprintTemplate(quality, template, IFingerIdentifier.valueOf(finger.name))

internal fun FingerprintTemplate.fromDomainToApi() = ApiFingerprintTemplate(quality, template, finger)
