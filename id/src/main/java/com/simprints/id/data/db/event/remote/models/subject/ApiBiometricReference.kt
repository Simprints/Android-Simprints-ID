package com.simprints.id.data.db.event.remote.models.subject

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import com.simprints.id.data.db.event.domain.models.subject.BiometricReference
import com.simprints.id.data.db.event.domain.models.subject.FaceTemplate
import com.simprints.id.data.db.event.domain.models.subject.FingerIdentifier
import com.simprints.id.data.db.event.domain.models.subject.FingerprintTemplate
import com.simprints.id.data.db.event.remote.models.subject.ApiBiometricReferenceType.FACE_REFERENCE
import com.simprints.id.data.db.event.remote.models.subject.ApiBiometricReferenceType.FINGERPRINT_REFERENCE
import com.simprints.id.data.db.event.domain.models.subject.FaceReference as DomainFaceReference
import com.simprints.id.data.db.event.domain.models.subject.FingerprintReference as DomainFingerprintReference

@Keep
sealed class ApiBiometricReference(@Transient val type: ApiBiometricReferenceType)

@Keep
class ApiFaceReference(val templates: List<ApiFaceTemplate>,
                       val metadata: HashMap<String, String>? = null): ApiBiometricReference(FACE_REFERENCE)

@Keep
class ApiFingerprintReference(val templates: List<ApiFingerprintTemplate>,
                              val metadata: HashMap<String, String>? = null): ApiBiometricReference(FINGERPRINT_REFERENCE)

@Keep
enum class ApiBiometricReferenceType() {
    @JsonProperty("FaceReference") FACE_REFERENCE,
    @JsonProperty("FingerprintReference") FINGERPRINT_REFERENCE;
}

fun BiometricReference.fromDomainToApi() = when (this) {
    is DomainFaceReference -> {
        ApiFaceReference(templates.map { it.fromDomainToApi() }, metadata)
    }
    is DomainFingerprintReference -> {
        ApiFingerprintReference(templates.map { it.fromDomainToApi() }, metadata)
    }
}

fun FaceTemplate.fromDomainToApi() = ApiFaceTemplate(template)

fun FingerprintTemplate.fromDomainToApi() =
    ApiFingerprintTemplate(quality, template, finger.fromDomainToApi())

fun FingerIdentifier.fromDomainToApi() = when(this) {
    FingerIdentifier.RIGHT_5TH_FINGER -> ApiFingerIdentifier.RIGHT_5TH_FINGER
    FingerIdentifier.RIGHT_4TH_FINGER -> ApiFingerIdentifier.RIGHT_4TH_FINGER
    FingerIdentifier.RIGHT_3RD_FINGER -> ApiFingerIdentifier.RIGHT_3RD_FINGER
    FingerIdentifier.RIGHT_INDEX_FINGER -> ApiFingerIdentifier.RIGHT_INDEX_FINGER
    FingerIdentifier.RIGHT_THUMB -> ApiFingerIdentifier.RIGHT_THUMB
    FingerIdentifier.LEFT_THUMB -> ApiFingerIdentifier.LEFT_THUMB
    FingerIdentifier.LEFT_INDEX_FINGER -> ApiFingerIdentifier.LEFT_INDEX_FINGER
    FingerIdentifier.LEFT_3RD_FINGER -> ApiFingerIdentifier.LEFT_3RD_FINGER
    FingerIdentifier.LEFT_4TH_FINGER -> ApiFingerIdentifier.LEFT_4TH_FINGER
    FingerIdentifier.LEFT_5TH_FINGER -> ApiFingerIdentifier.LEFT_5TH_FINGER
}



