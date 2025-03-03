package com.simprints.infra.enrolment.records.realm.store.migration.oldschemas

/**
 * Should add field format to new DbFaceSample and DbFingerprintSample
 */
internal object SubjectsSchemaV11 {
    const val FIELD_FORMAT: String = "format"

    const val FINGERPRINT_TABLE: String = "DbFingerprintSample"
    const val FACE_TABLE: String = "DbFaceSample"

    const val ISO_19794_2_FORMAT = "ISO_19794_2"
    const val RANK_ONE_1_23_FORMAT = "RANK_ONE"
}
