package com.simprints.id.data.db.event.remote.events.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.subject.BiometricReference
import com.simprints.id.data.db.event.domain.events.subject.FaceTemplate
import com.simprints.id.data.db.event.domain.events.subject.FingerIdentifier
import com.simprints.id.data.db.event.domain.events.subject.FingerprintTemplate
import com.simprints.id.data.db.event.remote.events.subject.ApiBiometricReferenceName.FACE_REFERENCE_SERIALISED
import com.simprints.id.data.db.event.remote.events.subject.ApiBiometricReferenceName.FINGERPRINT_REFERENCE_SERIALISED
import com.simprints.id.data.db.event.remote.events.subject.ApiBiometricReferenceType.FACE_REFERENCE
import com.simprints.id.data.db.event.remote.events.subject.ApiBiometricReferenceType.FINGERPRINT_REFERENCE
import com.simprints.id.data.db.event.domain.events.subject.FaceReference as DomainFaceReference
import com.simprints.id.data.db.event.domain.events.subject.FingerprintReference as DomainFingerprintReference

@Keep
sealed class ApiBiometricReference(@Transient val type: ApiBiometricReferenceType)

@Keep
class ApiFaceReference(val templates: List<ApiFaceTemplate>,
                       val metadata: HashMap<String, String>? = null): ApiBiometricReference(FACE_REFERENCE)

@Keep
class ApiFingerprintReference(val templates: List<ApiFingerprintTemplate>,
                              val metadata: HashMap<String, String>? = null): ApiBiometricReference(FINGERPRINT_REFERENCE)

@Keep
enum class ApiBiometricReferenceType(val apiName: String) {
    FACE_REFERENCE(FACE_REFERENCE_SERIALISED),
    FINGERPRINT_REFERENCE(FINGERPRINT_REFERENCE_SERIALISED);
}

private object ApiBiometricReferenceName{
    const val FACE_REFERENCE_SERIALISED = "FaceReference"
    const val FINGERPRINT_REFERENCE_SERIALISED = "FingerprintReference"
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



