package com.simprints.id.data.db.local.realm

import com.simprints.id.data.db.local.realm.models.rl_Fingerprint
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.rl_Project
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.domain.Constants
import io.realm.*
import io.realm.annotations.RealmModule
import java.util.*

internal class PeopleRealmMigration(val projectId: String) : RealmMigration {

    @RealmModule(classes = [rl_Fingerprint::class, rl_Person::class, rl_Project::class, rl_SyncInfo::class])
    class PeopleModule

    companion object {
        const val REALM_SCHEMA_VERSION: Long = 5

        const val PERSON_TABLE: String = "rl_Person"
        const val USER_TABLE: String = "rl_User"
        const val FINGERPRINT_TABLE: String = "rl_Fingerprint"
        const val API_KEY_TABLE: String = "rl_ApiKey"
        const val SYNC_INFO_TABLE: String = "rl_SyncInfo"
        const val PROJECT_TABLE: String = "rl_Project"

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

        const val FINGERPRINT_PERSON = "person"
    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        for (i in oldVersion..newVersion) {
            when (i.toInt()) {
                0 -> migrateTo1(realm.schema)
                1 -> migrateTo2(realm.schema)
                2 -> migrateTo3(realm.schema)
                3 -> migrateTo4(realm.schema)
                4 -> migrateTo5(realm.schema)
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
        migratePersonTo2(schema)
        migrateSyncInfoTo2(schema)
        migrateProjectInfoTo2(schema)

        schema.get(FINGERPRINT_TABLE)?.removeField(FINGERPRINT_PERSON)
        schema.remove(API_KEY_TABLE)
    }

    private fun migratePersonTo2(schema: RealmSchema) {
        schema.get(PERSON_TABLE)?.addField(PERSON_PROJECT_ID, String::class.java)?.transform {
            it.setString(PERSON_PROJECT_ID, projectId)
        }?.setRequired(PERSON_PROJECT_ID, true)

        // Workaround to make primary key required (kotlin not nullable)
        // https://github.com/realm/realm-java/issues/5235
        schema.get(PERSON_TABLE)?.run {
            if (isPrimaryKey(PERSON_PATIENT_ID) && !isRequired(PERSON_PATIENT_ID)) {
                removePrimaryKey()
                setRequired(PERSON_PATIENT_ID, true)
                addIndex(PERSON_PATIENT_ID)
                addPrimaryKey(PERSON_PATIENT_ID)
            }
        }

        schema.get(PERSON_TABLE)
            ?.setRequired(PERSON_USER_ID, true)
            ?.setRequired(PERSON_MODULE_ID, true)
            ?.addField(PERSON_CREATE_TIME_TEMP, Date::class.java)
            ?.removeField(PERSON_CREATE_TIME)
            ?.renameField(PERSON_CREATE_TIME_TEMP, PERSON_CREATE_TIME)
            ?.removeField(ANDROID_ID_FIELD)

        // It's null. The record is marked toSync = true, so having updatedAt = null is
        // even consistent with toSync = person.updatedAt == null || person.createdAt == null
        schema.get(PERSON_TABLE)?.addField(UPDATE_FIELD, Date::class.java)

        schema.get(PERSON_TABLE)?.addField(SYNC_FIELD, Boolean::class.java)?.transform {
            it.set(SYNC_FIELD, true)
        }
    }

    private fun migrateSyncInfoTo2(schema: RealmSchema) {
        schema.create(SYNC_INFO_TABLE)
            .addField(SYNC_INFO_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
            .addDateAndMakeRequired(SYNC_INFO_LAST_UPDATE)
            .addStringAndMakeRequired(SYNC_INFO_LAST_PATIENT_ID)
            .addDateAndMakeRequired(SYNC_INFO_SYNC_TIME)
    }

    private fun migrateProjectInfoTo2(schema: RealmSchema) {
        schema.create(PROJECT_TABLE)
            .addField(PROJECT_ID, String::class.java, FieldAttribute.PRIMARY_KEY).setRequired(PROJECT_ID, true)
            .addStringAndMakeRequired(PROJECT_LEGACY_ID)
            .addStringAndMakeRequired(PROJECT_NAME)
            .addStringAndMakeRequired(PROJECT_DESCRIPTION)
            .addStringAndMakeRequired(PROJECT_CREATOR)
            .addStringAndMakeRequired(PROJECT_UPDATED_AT)
    }

    private fun migrateTo3(schema: RealmSchema) {
        schema.get(PERSON_TABLE)?.transform {
            it.set(SYNC_FIELD, true)
        }
    }

    private fun migrateTo4(schema: RealmSchema) {
        schema.get(SYNC_INFO_TABLE)
            ?.addField(SYNC_INFO_MODULE_ID, String::class.java)
    }

    private fun migrateTo5(schema: RealmSchema) {
        //We want to delete RlSyncInfo, but we need to migrate to Room.
        //In the next version, we will drop this class.
    }

    private fun RealmObjectSchema.addStringAndMakeRequired(name: String): RealmObjectSchema =
        this.addField(name, String::class.java).setRequired(name, true)

    private fun RealmObjectSchema.addDateAndMakeRequired(name: String): RealmObjectSchema =
        this.addField(name, Date::class.java).setRequired(name, true)

    override fun hashCode(): Int {
        return PeopleRealmMigration.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is PeopleRealmMigration
    }
}
