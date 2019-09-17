package com.simprints.id.data.db.common.realm.oldschemas

object PeopleSchemaV6{
    const val PERSON_TABLE: String = "DbPerson"
    const val FINGERPRINT_TABLE: String = "DbFingerprint"
    const val SYNC_INFO_TABLE: String = "DbSyncInfo"
    const val PROJECT_TABLE: String = "DbProject"

    const val MODULE_FIELD: String = "moduleId"
    const val UPDATE_FIELD: String = "updatedAt"
    const val SYNC_FIELD: String = "toSync"
    const val ANDROID_ID_FIELD: String = "androidId"
    const val SYNC_INFO_ID: String = "syncGroupId"
    const val SYNC_INFO_MODULE_ID: String = "moduleId"
    const val SYNC_INFO_LAST_UPDATE: String = "lastKnownPatientUpdatedAt"
    const val SYNC_INFO_LAST_PATIENT_ID: String = "lastKnownPatientId"
    const val SYNC_INFO_SYNC_TIME: String = "lastSyncTime"

    const val PROJECT_ID = "id"
    const val PROJECT_LEGACY_ID = "legacyId"
    const val PROJECT_NAME = "name"
    const val PROJECT_DESCRIPTION = "description"
    const val PROJECT_CREATOR = "creator"
    const val PROJECT_UPDATED_AT = "updatedAt"

    const val PERSON_PROJECT_ID = "projectId"
    const val PERSON_PATIENT_ID = "patientId"
    const val PERSON_MODULE_ID = "moduleId"
    const val PERSON_USER_ID = "userId"
    const val PERSON_CREATE_TIME_TEMP = "createdAt_tmp"
    const val PERSON_CREATE_TIME = "createdAt"
    const val PERSON_FIELD_FINGERPRINT_SAMPLES = "fingerprints"

    const val FINGERPRINT_PERSON = "person"
    const val FINGERPRINT_FIELD_FINGER_IDENTIFIER = "fingerId"
    val FINGERPRINT_FIELD_TEMPLATE_QUALITY_SCORE: String = "qualityScore"
}
