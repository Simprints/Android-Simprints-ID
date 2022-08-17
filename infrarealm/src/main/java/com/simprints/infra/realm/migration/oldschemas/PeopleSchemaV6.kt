package com.simprints.infra.realm.migration.oldschemas

internal object PeopleSchemaV6{
    const val PERSON_TABLE = "DbPerson"
    const val FINGERPRINT_TABLE: String = "DbFingerprint"
    const val PROJECT_ID = "id"
    const val PERSON_FIELD_FINGERPRINT_SAMPLES = "fingerprints"

    const val FINGERPRINT_FIELD_FINGER_IDENTIFIER = "fingerId"
    const val FINGERPRINT_FIELD_TEMPLATE_QUALITY_SCORE: String = "qualityScore"
}
