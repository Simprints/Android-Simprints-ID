package com.simprints.id.data.db.common.realm.oldschemas

object PeopleSchemaV7 {

    const val FINGERPRINT_TABLE: String = "DbFingerprintSample"
    const val FINGERPRINT_FIELD_IMAGEREF: String = "imageRef"
    const val FINGERPRINT_FIELD_TEMPLATE: String = "template"
    const val FINGERPRINT_FIELD_ID: String = "id"
    const val FINGERPRINT_FIELD_FINGER_IDENTIFIER: String = "fingerIdentifier"
    const val FINGERPRINT_FIELD_TEMPLATE_QUALITY_SCORE: String = "templateQualityScore"

    const val PERSON_TABLE: String = "DbPerson"
    const val PERSON_FIELD_FACE_SAMPLES = "faceSamples"
    const val PERSON_FIELD_FINGERPRINT_SAMPLES = "fingerprintSamples"

    const val FACE_TABLE: String = "DbFaceSample"
    const val FACE_FIELD_ID: String = "id"
    const val FACE_FIELD_TEMPLATE: String = "template"
    const val FACE_FIELD_IMAGEREF: String = "imageRef"

}
