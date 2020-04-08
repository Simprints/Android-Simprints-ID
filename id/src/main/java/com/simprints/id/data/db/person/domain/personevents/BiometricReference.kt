package com.simprints.id.data.db.person.domain.personevents

sealed class BiometricReference(val type: BiometricReferenceType)

class FaceReference(val metadata: HashMap<String, String>,
                    val templates: Array<FaceTemplate>): BiometricReference(BiometricReferenceType.FaceReference)

class FingerprintReference(val metadata: HashMap<String, String>,
                           val templates: Array<FingerprintTemplate>): BiometricReference(BiometricReferenceType.FingerprintReference)

class FaceTemplate(val template: String)

class FingerprintTemplate(val quality: Int,
                          val template: String,
                          val finger: FingerIdentifier)

enum class BiometricReferenceType {
    FaceReference,
    FingerprintReference
}

enum class FingerIdentifier {
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
