package com.simprints.id.data.db.common.realm.oldschemas

object PeopleSchemaV2 {
    const val UPDATE_FIELD: String = "updatedAt"
    const val ANDROID_ID_FIELD: String = "androidId"
    const val SYNC_INFO_LAST_UPDATE: String = "lastKnownPatientUpdatedAt"
    const val SYNC_INFO_LAST_PATIENT_ID: String = "lastKnownPatientId"
    const val SYNC_INFO_SYNC_TIME: String = "lastSyncTime"
    const val SYNC_INFO_ID: String = "syncGroupId"

    const val PERSON_PROJECT_ID = "projectId"
    const val PERSON_PATIENT_ID = "patientId"
    const val PERSON_MODULE_ID = "moduleId"
    const val PERSON_USER_ID = "userId"
    const val PERSON_CREATE_TIME_TEMP = "createdAt_tmp"
    const val PERSON_CREATE_TIME = "createdAt"

    const val SYNC_FIELD: String = "toSync"
}
