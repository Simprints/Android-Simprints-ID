package com.simprints.infra.enrolment.records.realm.store.migration

import com.simprints.infra.enrolment.records.realm.store.migration.oldschemas.SubjectsSchemaV10
import com.simprints.infra.enrolment.records.realm.store.migration.oldschemas.SubjectsSchemaV11
import com.simprints.infra.enrolment.records.realm.store.migration.oldschemas.SubjectsSchemaV12
import com.simprints.infra.enrolment.records.realm.store.migration.oldschemas.SubjectsSchemaV13
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.toRealmInstant
import io.realm.kotlin.dynamic.DynamicMutableRealmObject
import io.realm.kotlin.dynamic.getValue
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.types.RealmUUID
import java.util.Date
import java.util.UUID

internal class RealmMigrations : AutomaticSchemaMigration {
    override fun migrate(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        val oldVersion = migrationContext.oldRealm.schemaVersion()
        val newVersion = migrationContext.newRealm.schemaVersion()

        for (i in oldVersion until newVersion) {
            when (i.toInt()) {
                9 -> migrateTo10(migrationContext)
                10 -> migrateTo11(migrationContext)
                11 -> migrateTo12(migrationContext)
                12 -> migrateTo13(migrationContext)
            }
        }
    }

    private fun migrateTo10(migrationContext: AutomaticSchemaMigration.MigrationContext) = with(SubjectsSchemaV10) {
        val newRealm = migrationContext.newRealm
        migrationContext.enumerate(PERSON_TABLE) { oldObject, _ ->
            newRealm.copyToRealm(
                DynamicMutableRealmObject.create(
                    type = SUBJECT_TABLE,
                    properties = mapOf(
                        // Some fields are renamed
                        SUBJECT_ID to oldObject.getValue<String>(PERSON_PATIENT_ID),
                        ATTENDANT_ID to oldObject.getValue(PERSON_USER_ID),
                        // Rest do not change names
                        PERSON_PROJECT_ID to oldObject.getValue(PERSON_PROJECT_ID),
                        PERSON_MODULE_ID to oldObject.getValue(PERSON_MODULE_ID),
                        PERSON_CREATE_TIME to oldObject.getNullableValue(PERSON_CREATE_TIME, Date::class)?.toRealmInstant(),
                        PERSON_UPDATE_TIME to oldObject.getNullableValue(PERSON_UPDATE_TIME, Date::class)?.toRealmInstant(),
                        PERSON_TO_SYNC to oldObject.getValue(PERSON_TO_SYNC),
                        PERSON_FINGERPRINT_SAMPLES to oldObject.getValueList(PERSON_FINGERPRINT_SAMPLES, DbFingerprintSample::class),
                        PERSON_FACE_SAMPLES to oldObject.getValueList(PERSON_FACE_SAMPLES, DbFaceSample::class),
                    ),
                ),
            )
        }
    }

    private fun migrateTo11(migrationContext: AutomaticSchemaMigration.MigrationContext) = with(SubjectsSchemaV11) {
        migrationContext.enumerate(FINGERPRINT_TABLE) { _, newObject ->
            newObject?.set(FIELD_FORMAT, ISO_19794_2_FORMAT)
        }

        migrationContext.enumerate(FACE_TABLE) { _, newObject ->
            newObject?.set(FIELD_FORMAT, RANK_ONE_1_23_FORMAT)
        }
    }

    /* Because of a previous bug in Realm the @PrimaryKey annotation in DbFaceSample and
       DbFingerprintSample wasn't taken into account and the tables effectively didn't have primary
       keys. This leads to duplication of rows in some cases. To fix first we need to deduplicate
       the tables and then set ID fields as primary keys.
     */
    private fun migrateTo12(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        updateDuplicatedSampleIds(migrationContext)
    }

    // In some projects there are duplicate records that are not orphans. Reason is still unclear
    // but probably two subjects point to the same sample id which is inserted twice in the table
    // For such cases we scan the two tables for duplicate ids and change them before making id
    // the primary key
    private fun updateDuplicatedSampleIds(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        val oldRealm = migrationContext.oldRealm
        migrationContext.enumerate(SubjectsSchemaV11.FINGERPRINT_TABLE) { oldObject, newObject ->
            val id = oldObject.getValue<String>(SubjectsSchemaV12.FINGERPRINT_FIELD_ID)
            val count = oldRealm
                .query(
                    SubjectsSchemaV11.FINGERPRINT_TABLE,
                    "${SubjectsSchemaV12.FINGERPRINT_FIELD_ID} == $0",
                    id,
                ).count()
                .find()
            if (count > 1) {
                newObject?.set(SubjectsSchemaV12.FINGERPRINT_FIELD_ID, UUID.randomUUID().toString())
            }
        }
        migrationContext.enumerate(SubjectsSchemaV11.FACE_TABLE) { oldObject, newObject ->
            val id = oldObject.getValue<String>(SubjectsSchemaV12.FACE_FIELD_ID)
            val count = oldRealm.query(SubjectsSchemaV11.FACE_TABLE, "${SubjectsSchemaV12.FACE_FIELD_ID} == $0", id).count().find()
            if (count > 1) {
                newObject?.set(SubjectsSchemaV12.FACE_FIELD_ID, UUID.randomUUID().toString())
            }
        }
    }

    private fun migrateTo13(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        migrationContext.enumerate(SubjectsSchemaV13.SUBJECT_TABLE) { oldObject, newObject ->
            val subjectId = oldObject.getValue<String>(SubjectsSchemaV13.SUBJECT_ID_FIELD)
            newObject?.set(SubjectsSchemaV13.SUBJECT_ID_FIELD, RealmUUID.from(subjectId))
        }
    }
}
