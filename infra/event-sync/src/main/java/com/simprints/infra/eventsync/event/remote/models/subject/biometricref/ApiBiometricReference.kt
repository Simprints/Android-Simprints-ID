package com.simprints.infra.eventsync.event.remote.models.subject.biometricref

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.face.ApiFaceReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.face.ApiFaceTemplate
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.infra.eventsync.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import com.simprints.infra.events.event.domain.models.subject.FaceReference as DomainFaceReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference as DomainFingerprintReference

private const val FACE_REFERENCE_KEY = "FaceReference"
private const val FINGERPRINT_REFERENCE_KEY = "FingerprintReference"

@ExcludedFromGeneratedTestCoverageReports("API model")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    // [MS-1076] Annotations should have 'looseHandling' parameter passed to it to allow loose type handling of the nested fields (metadata)
    // @JsonSubTypes.Type([...], looseHandling = true) once we update to SDK => 25 and Jackson => 2.16.0.
    // Ref: https://github.com/FasterXML/jackson-databind/issues/3877#issuecomment-1508508854
    JsonSubTypes.Type(value = ApiFaceReference::class, name = FACE_REFERENCE_KEY),
    JsonSubTypes.Type(value = ApiFingerprintReference::class, name = FINGERPRINT_REFERENCE_KEY),
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
}

internal fun ApiBiometricReference.fromApiToDomain() = when (this.type) {
    ApiBiometricReferenceType.FaceReference -> (this as ApiFaceReference).fromApiToDomain()
    ApiBiometricReferenceType.FingerprintReference -> (this as ApiFingerprintReference).fromApiToDomain()
}

internal fun ApiFaceReference.fromApiToDomain() = DomainFaceReference(id, templates.map { it.fromApiToDomain() }, format, metadata)

internal fun ApiFaceTemplate.fromApiToDomain() = FaceTemplate(template)

internal fun FaceTemplate.fromDomainToApi() = ApiFaceTemplate(template)

internal fun ApiFingerprintReference.fromApiToDomain() =
    DomainFingerprintReference(id, templates.map { it.fromApiToDomain() }, format, metadata)

internal fun ApiFingerprintTemplate.fromApiToDomain() = FingerprintTemplate(template, SampleIdentifier.valueOf(finger.name))
