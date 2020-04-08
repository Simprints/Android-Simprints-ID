package com.simprints.id.data.db.person.remote.models.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.personevents.BiometricReference
import com.simprints.id.data.db.person.domain.personevents.FaceTemplate
import com.simprints.id.data.db.person.domain.personevents.FingerIdentifier
import com.simprints.id.data.db.person.domain.personevents.FingerprintTemplate
import com.simprints.id.data.db.person.remote.models.personevents.ApiBiometricReferenceType.FaceReference
import com.simprints.id.data.db.person.remote.models.personevents.ApiBiometricReferenceType.FingerprintReference
import com.simprints.id.data.db.person.domain.personevents.FaceReference as DomainFaceReference
import com.simprints.id.data.db.person.domain.personevents.FingerprintReference as DomainFingerprintReference

@Keep
sealed class ApiBiometricReference(val type: ApiBiometricReferenceType)

@Keep
class ApiFaceReference(val metadata: HashMap<String, String>,
                       val templates: List<ApiFaceTemplate>): ApiBiometricReference(FaceReference)

@Keep
class ApiFingerprintReference(val metadata: HashMap<String, String>,
                              val templates: List<ApiFingerprintTemplate>): ApiBiometricReference(FingerprintReference)

@Keep
enum class ApiBiometricReferenceType {
    FaceReference,
    FingerprintReference
}

fun BiometricReference.fromDomainToApi() = when (this) {
    is DomainFaceReference -> {
        ApiFaceReference(metadata, templates.map { it.fromDomainToApi() })
    }
    is DomainFingerprintReference -> {
        ApiFingerprintReference(metadata, templates.map { it.fromDomainToApi() })
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



