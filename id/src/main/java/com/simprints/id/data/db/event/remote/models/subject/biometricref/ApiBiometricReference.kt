package com.simprints.id.data.db.event.remote.models.subject.biometricref

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.id.data.db.event.domain.models.subject.BiometricReference
import com.simprints.id.data.db.event.domain.models.subject.FaceTemplate
import com.simprints.id.data.db.event.domain.models.subject.FingerIdentifier
import com.simprints.id.data.db.event.domain.models.subject.FingerprintTemplate
import com.simprints.id.data.db.event.remote.models.subject.biometricref.face.ApiFaceReference
import com.simprints.id.data.db.event.remote.models.subject.biometricref.face.ApiFaceTemplate
import com.simprints.id.data.db.event.domain.models.face.FaceTemplateFormat.RANK_ONE_1_23
import com.simprints.id.data.db.event.remote.models.subject.biometricref.fingerprint.ApiFingerIdentifier.*
import com.simprints.id.data.db.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintReference
import com.simprints.id.data.db.event.remote.models.subject.biometricref.fingerprint.ApiFingerprintTemplate
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintTemplateFormat.ISO_19794_2
import com.simprints.id.data.db.event.domain.models.subject.FaceReference as DomainFaceReference
import com.simprints.id.data.db.event.domain.models.subject.FingerprintReference as DomainFingerprintReference

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
        ApiFaceReference(id, templates.map { it.fromDomainToApi() }, RANK_ONE_1_23, metadata)
    }
    is DomainFingerprintReference -> {
        ApiFingerprintReference(id, templates.map { it.fromDomainToApi() }, ISO_19794_2, metadata)
    }
}

fun FaceTemplate.fromDomainToApi() = ApiFaceTemplate(template)

fun FingerprintTemplate.fromDomainToApi() =
    ApiFingerprintTemplate(quality, template, finger.fromDomainToApi())

fun FingerIdentifier.fromDomainToApi() = when (this) {
    FingerIdentifier.RIGHT_5TH_FINGER -> RIGHT_5TH_FINGER
    FingerIdentifier.RIGHT_4TH_FINGER -> RIGHT_4TH_FINGER
    FingerIdentifier.RIGHT_3RD_FINGER -> RIGHT_3RD_FINGER
    FingerIdentifier.RIGHT_INDEX_FINGER -> RIGHT_INDEX_FINGER
    FingerIdentifier.RIGHT_THUMB -> RIGHT_THUMB
    FingerIdentifier.LEFT_THUMB -> LEFT_THUMB
    FingerIdentifier.LEFT_INDEX_FINGER -> LEFT_INDEX_FINGER
    FingerIdentifier.LEFT_3RD_FINGER -> LEFT_3RD_FINGER
    FingerIdentifier.LEFT_4TH_FINGER -> LEFT_4TH_FINGER
    FingerIdentifier.LEFT_5TH_FINGER -> LEFT_5TH_FINGER
}



