package com.simprints.id.data.db.person.remote.models.personevents

import com.simprints.id.data.db.person.remote.models.personevents.ApiBiometricReferenceType.FaceReference
import com.simprints.id.data.db.person.remote.models.personevents.ApiBiometricReferenceType.FingerprintReference

sealed class ApiBiometricReference(val type: ApiBiometricReferenceType)

class ApiFaceReference(val metadata: String,
                       val templates: Array<ApiFaceTemplate>): ApiBiometricReference(FaceReference)

class ApiFingerprintReference(val metadata: String,
                              val templates: Array<ApiFingerprintTemplate>): ApiBiometricReference(FingerprintReference)

class ApiFaceTemplate(val template: String)

class ApiFingerprintTemplate(val quality: Int,
                             val template: String,
                             val finger: ApiFingerIdentifier)

enum class ApiBiometricReferenceType {
    FaceReference,
    FingerprintReference
}

enum class ApiFingerIdentifier {
    RIGHT_5TH_FINGER,
    RIGHT_4TH_FINGER,
    RIGHT_3RD_FINGER,
    RIGHT_INDEX_FINGER,
    RIGHT_THUMB,
    LEFT_THUMB,
    LEFT_INDEX_FINGER,
    LEFT_3RD_FINGER,
    LEFT_4TH_FINGER,
    LEFT_5TH_FINGER
}
