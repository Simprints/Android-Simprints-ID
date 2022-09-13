package com.simprints.infra.realm.migration

import com.simprints.infra.realm.migration.oldschemas.*
import io.realm.*
import io.realm.FieldAttribute.REQUIRED
import java.util.*

internal class RealmMigrations(private val projectId: String) : RealmMigration {

    companion object {

        private const val FINGERPRINT_TABLE: String = "DbFingerprint"
        private const val SYNC_INFO_TABLE: String = "DbSyncInfo"
        private const val PROJECT_TABLE: String = "DbProject"

        private const val GLOBAL_ID = "GLOBAL-2b14bf72-b68a-4c24-acaf-66d5e1fcc4bc"
        private const val PROJECT_ID = "id"
        private const val PROJECT_LEGACY_ID = "legacyId"
        private const val PROJECT_NAME = "name"
        private const val PROJECT_DESCRIPTION = "description"
        private const val PROJECT_CREATOR = "creator"
        private const val IMAGE_BUCKET = "imageBucket"
        private const val PROJECT_UPDATED_AT = "updatedAt"

        private const val FINGERPRINT_PERSON = "person"

        private const val ISO_19794_2_FORMAT = "ISO_19794_2"
        private const val RANK_ONE_1_23_FORMAT = "RANK_ONE"

    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        for (i in oldVersion..newVersion) {
            when (i.toInt()) {
                0 -> migrateTo1(realm.schema)
                1 -> migrateTo2(realm.schema)
                2 -> migrateTo3(realm.schema)
                3 -> migrateTo4(realm.schema)
                4 -> migrateTo5()
                5 -> migrateTo6(realm.schema)
                6 -> migrateTo7(realm.schema)
                7 -> migrateTo8(realm.schema)
                8 -> migrateTo9(realm.schema)
                9 -> migrateTo10(realm.schema)
                10 -> migrateTo11(realm.schema)
                11 -> migrateTo12(realm.schema)
                12 -> migrateTo13(realm.schema)
            }
        }
    }

    private fun migrateTo1(schema: RealmSchema) {
        with(PeopleSchemaV1) {
            schema.get(PERSON_TABLE)?.addField(MODULE_FIELD, String::class.java)?.transform {
                it.set(MODULE_FIELD, GLOBAL_ID)
            }

            schema.remove("rl_User")
        }
    }

    private fun migrateTo2(schema: RealmSchema) {
        migratePersonTo2(schema)
        migrateSyncInfoTo2(schema)
        migrateProjectInfoTo2(schema)

        schema.get(FINGERPRINT_TABLE)?.removeField(FINGERPRINT_PERSON)
        schema.remove("rl_ApiKey")
    }

    private fun migratePersonTo2(schema: RealmSchema) {
        with(PeopleSchemaV2) {
            schema.get(PeopleSchemaV1.PERSON_TABLE)?.addField(PERSON_PROJECT_ID, String::class.java)
                ?.transform {
                    it.setString(PERSON_PROJECT_ID, projectId)
                }?.setRequired(PERSON_PROJECT_ID, true)

            // Workaround to make primary key required (kotlin not nullable)
            // https://github.com/realm/realm-java/issues/5235
            schema.get(PeopleSchemaV1.PERSON_TABLE)?.run {
                if (isPrimaryKey(PERSON_PATIENT_ID) && !isRequired(PERSON_PATIENT_ID)) {
                    removePrimaryKey()
                    setRequired(PERSON_PATIENT_ID, true)
                    addIndex(PERSON_PATIENT_ID)
                    addPrimaryKey(PERSON_PATIENT_ID)
                }
            }

            schema.get(PeopleSchemaV1.PERSON_TABLE)
                ?.setRequired(PERSON_USER_ID, true)
                ?.setRequired(PERSON_MODULE_ID, true)
                ?.addField(PERSON_CREATE_TIME_TEMP, Date::class.java)
                ?.removeField(PERSON_CREATE_TIME)
                ?.renameField(PERSON_CREATE_TIME_TEMP, PERSON_CREATE_TIME)
                ?.removeField(ANDROID_ID_FIELD)

            // It's null. The record is marked toSync = true, so having updatedAt = null is
            // even consistent with toSync = person.updatedAt == null || person.createdAt == null
            schema.get(PeopleSchemaV1.PERSON_TABLE)?.addField(UPDATE_FIELD, Date::class.java)

            schema.get(PeopleSchemaV1.PERSON_TABLE)?.addField(SYNC_FIELD, Boolean::class.java)
                ?.transform {
                    it.set(SYNC_FIELD, true)
                }
        }
    }

    private fun migrateSyncInfoTo2(schema: RealmSchema) {
        with(PeopleSchemaV2) {
            schema.create(SYNC_INFO_TABLE)
                .addField(SYNC_INFO_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
                .addDateAndMakeRequired(SYNC_INFO_LAST_UPDATE)
                .addStringAndMakeRequired(SYNC_INFO_LAST_PATIENT_ID)
                .addDateAndMakeRequired(SYNC_INFO_SYNC_TIME)
        }
    }

    private fun migrateProjectInfoTo2(schema: RealmSchema) {
        schema.create(PROJECT_TABLE)
            .addField(PROJECT_ID, String::class.java, FieldAttribute.PRIMARY_KEY).setRequired(
                PROJECT_ID, true
            )
            .addStringAndMakeRequired(PROJECT_LEGACY_ID)
            .addStringAndMakeRequired(PROJECT_NAME)
            .addStringAndMakeRequired(PROJECT_DESCRIPTION)
            .addStringAndMakeRequired(PROJECT_CREATOR)
            .addStringAndMakeRequired(PROJECT_UPDATED_AT)
    }

    private fun migrateTo3(schema: RealmSchema) {
        with(PeopleSchemaV3) {
            schema.get(PeopleSchemaV1.PERSON_TABLE)?.transform {
                it.set(SYNC_FIELD, true)
            }
        }
    }

    private fun migrateTo4(schema: RealmSchema) {
        with(PeopleSchemaV4) {
            schema.get(SYNC_INFO_TABLE)?.addField(SYNC_INFO_MODULE_ID, String::class.java)
        }
    }

    private fun migrateTo5() {
        //We want to delete DbSyncInfo, but we need to migrate to Room.
        //We do the migration in DownSyncTask
        //In the next version, we will drop this class.
    }

    private fun migrateTo6(schema: RealmSchema) {
        schema.rename(PeopleSchemaV1.PERSON_TABLE, PeopleSchemaV6.PERSON_TABLE)
        schema.rename(PeopleSchemaV5.FINGERPRINT_TABLE, FINGERPRINT_TABLE)
        schema.rename(PeopleSchemaV5.PROJECT_TABLE, PROJECT_TABLE)
        schema.get(PROJECT_TABLE)?.removeField(PROJECT_LEGACY_ID)

        schema.rename(PeopleSchemaV5.SYNC_INFO_TABLE, SYNC_INFO_TABLE)
    }

    private fun migrateTo7(schema: RealmSchema) {
        with(PeopleSchemaV7) {
            val faceSamplesScheme = schema.create(FACE_TABLE)
                .addNewField<String>(FACE_FIELD_ID, REQUIRED)
                .addNewField<ByteArray>(FACE_FIELD_TEMPLATE, REQUIRED)

            schema.rename(PeopleSchemaV6.FINGERPRINT_TABLE, FINGERPRINT_TABLE)
                .addNewField<String>(FINGERPRINT_FIELD_ID, REQUIRED)
                .renameField(
                    PeopleSchemaV6.FINGERPRINT_FIELD_FINGER_IDENTIFIER,
                    FINGERPRINT_FIELD_FINGER_IDENTIFIER
                )
                .renameField(
                    PeopleSchemaV6.FINGERPRINT_FIELD_TEMPLATE_QUALITY_SCORE,
                    FINGERPRINT_FIELD_TEMPLATE_QUALITY_SCORE
                )
                .markAsRequired(FINGERPRINT_FIELD_TEMPLATE)

            schema.get(PERSON_TABLE)
                ?.renameField(
                    PeopleSchemaV6.PERSON_FIELD_FINGERPRINT_SAMPLES,
                    PERSON_FIELD_FINGERPRINT_SAMPLES
                )
                ?.addRealmListField(PERSON_FIELD_FACE_SAMPLES, faceSamplesScheme)
        }
    }

    private fun migrateTo8(schema: RealmSchema) {
        try {
            schema.remove(SYNC_INFO_TABLE)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun migrateTo9(schema: RealmSchema) {
        schema.get(PROJECT_TABLE)?.addNewField<String>(IMAGE_BUCKET, REQUIRED)
    }

    private fun migrateTo10(schema: RealmSchema) {
        schema.rename(PeopleSchemaV6.PERSON_TABLE, SubjectsSchemaV10.SUBJECT_TABLE)
            .renameField(PeopleSchemaV9.PERSON_PATIENT_ID_FIELD, SubjectsSchemaV10.SUBJECT_ID)
            .renameField(PeopleSchemaV9.PERSON_USER_ID_FIELD, SubjectsSchemaV10.ATTENDANT_ID)
    }

    private fun migrateTo11(schema: RealmSchema) {
        schema.get(PeopleSchemaV7.FINGERPRINT_TABLE)
            ?.addStringAndMakeRequired(SubjectsSchemaV11.FIELD_FORMAT)
            ?.transform {
                it.set(SubjectsSchemaV11.FIELD_FORMAT, ISO_19794_2_FORMAT)
            }

        schema.get(PeopleSchemaV7.FACE_TABLE)
            ?.addStringAndMakeRequired(SubjectsSchemaV11.FIELD_FORMAT)
            ?.transform {
                it.set(SubjectsSchemaV11.FIELD_FORMAT, RANK_ONE_1_23_FORMAT)
            }
    }

    private fun migrateTo12(schema: RealmSchema) {
        schema.get(PeopleSchemaV7.FINGERPRINT_TABLE)
            ?.addPrimaryKey(PeopleSchemaV7.FINGERPRINT_FIELD_ID)

        schema.get(PeopleSchemaV7.FACE_TABLE)
            ?.addPrimaryKey(PeopleSchemaV7.FACE_FIELD_ID)
    }

    private fun migrateTo13(schema: RealmSchema) {
        schema
            .get(SubjectsSchemaV13.SUBJECT_TABLE)
            ?.addField(SubjectsSchemaV13.TMP_SUBJECT_ID_FIELD, UUID::class.java)
            ?.transform { obj: DynamicRealmObject ->
                val subjectId = obj.getString(SubjectsSchemaV13.SUBJECT_ID_FIELD)
                obj.setUUID(SubjectsSchemaV13.TMP_SUBJECT_ID_FIELD, UUID.fromString(subjectId))
            }
            ?.removeField(SubjectsSchemaV13.SUBJECT_ID_FIELD)
            ?.renameField(
                SubjectsSchemaV13.TMP_SUBJECT_ID_FIELD,
                SubjectsSchemaV13.SUBJECT_ID_FIELD
            )
            ?.addPrimaryKey(SubjectsSchemaV13.SUBJECT_ID_FIELD)
    }


    private inline fun <reified T> RealmObjectSchema.addNewField(
        name: String,
        vararg attributes: FieldAttribute
    ): RealmObjectSchema =
        this.addField(name, T::class.java, *attributes)

    private fun RealmObjectSchema.addStringAndMakeRequired(name: String): RealmObjectSchema =
        this.addField(name, String::class.java).setRequired(name, true)

    private fun RealmObjectSchema.addDateAndMakeRequired(name: String): RealmObjectSchema =
        this.addField(name, Date::class.java).setRequired(name, true)

    private fun RealmObjectSchema.markAsRequired(name: String): RealmObjectSchema =
        this.setRequired(name, true)

    override fun hashCode(): Int {
        return RealmMigrations.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is RealmMigrations
    }
}
