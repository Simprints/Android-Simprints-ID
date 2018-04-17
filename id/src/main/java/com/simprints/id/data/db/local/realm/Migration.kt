package com.simprints.id.data.db.local.realm

import com.simprints.id.domain.Constants

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import io.realm.RealmSchema
import java.util.*


internal class Migration : RealmMigration {

    companion object {
        const val REALM_SCHEMA_VERSION: Long = 2

        const val PERSON_TABLE: String = "rl_Person"
        const val USER_TABLE: String = "rl_User"
        const val API_KEY_TABLE: String = "rl_ApiKey"
        const val SYNC_INFO_TABLE: String = "rl_SyncInfo"
        const val PROJECT_TABLE: String = "rl_Project"

        const val MODULE_FIELD: String = "moduleId"
        const val UPDATE_FIELD: String = "updatedAt"
        const val SYNC_FIELD: String = "toSync"
        const val ANDROID_ID_FIELD: String = "androidId"
        const val SYNC_INFO_ID: String = "syncGroupId"
        const val SYNC_INFO_LAST_UPDATE: String = "lastKnownPatientUpdatedAt"
        const val SYNC_INFO_LAST_PATIENT_ID: String = "lastKnownPatientId"
        const val SYNC_INFO_SYNC_TIME: String = "lastSyncTime"

        const val PROJECT_ID = "id"
        const val PROJECT_LEGACY_ID = "legacyId"
        const val PROJECT_NAME = "name"
        const val PROJECT_DESCRIPTION = "description"
        const val PROJECT_CREATOR = "creator"
        const val PROJECT_UPDATED_AT = "updatedAt"
    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        for (i in oldVersion..newVersion) {
            when (i.toInt()) {
                0 -> migrateTo1(realm.schema)
                1 -> migrateTo2(realm.schema)
            }
        }

    }

    private fun migrateTo1(schema: RealmSchema) {
        schema.get(PERSON_TABLE)?.addField(MODULE_FIELD, String::class.java)?.transform {
            it.set(MODULE_FIELD, Constants.GLOBAL_ID)
        }

        schema.remove(USER_TABLE)
    }

    private fun migrateTo2(schema: RealmSchema) {
        schema.get(PERSON_TABLE)?.addField(UPDATE_FIELD, Date::class.java)
        schema.get(PERSON_TABLE)?.addField(SYNC_FIELD, Boolean::class.java)?.transform {
            it.set(SYNC_FIELD, false)
        }
        schema.get(PERSON_TABLE)?.removeField(ANDROID_ID_FIELD)

        schema.remove(API_KEY_TABLE)

        schema.create(SYNC_INFO_TABLE)
            .addField(SYNC_INFO_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
            .addField(SYNC_INFO_LAST_UPDATE, Date::class.java)
            .addField(SYNC_INFO_LAST_PATIENT_ID, String::class.java)
            .addField(SYNC_INFO_SYNC_TIME, Date::class.java)

        schema.create(PROJECT_TABLE)
            .addField(PROJECT_ID, String::class.java, FieldAttribute.PRIMARY_KEY)
            .addField(PROJECT_LEGACY_ID, String::class.java)
            .addField(PROJECT_NAME, String::class.java)
            .addField(PROJECT_DESCRIPTION, String::class.java)
            .addField(PROJECT_CREATOR, String::class.java)
            .addField(PROJECT_UPDATED_AT, String::class.java)
    }

    override fun hashCode(): Int {
        return Migration.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is Migration
    }

}
