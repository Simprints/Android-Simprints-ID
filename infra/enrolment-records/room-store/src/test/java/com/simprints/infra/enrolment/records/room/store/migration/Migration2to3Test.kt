package com.simprints.infra.enrolment.records.room.store.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration2to3Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SubjectsDatabase::class.java,
    )

    @Test
    fun `when migrating from 2 to 3 then empty referenceIds are backfilled per subject and modality`() {
        val db2 = helper.createDatabase(name = TEST_DB, version = 2)
        insertSubject(db2, SUBJECT_1)
        insertSubject(db2, SUBJECT_2)

        // Subject 1 has two fingerprint templates with an empty referenceId (should share the same new id)
        insertTemplate(db2, uuid = "t1", subjectId = SUBJECT_1, referenceId = "", modality = FINGERPRINT_MODALITY)
        insertTemplate(db2, uuid = "t2", subjectId = SUBJECT_1, referenceId = "", modality = FINGERPRINT_MODALITY)
        // Subject 1 also has a face template with an empty referenceId (different modality -> different new id)
        insertTemplate(db2, uuid = "t3", subjectId = SUBJECT_1, referenceId = "", modality = FACE_MODALITY)
        // Subject 2 already has a valid referenceId which must not be touched
        insertTemplate(db2, uuid = "t4", subjectId = SUBJECT_2, referenceId = "existing-ref", modality = FINGERPRINT_MODALITY)

        val db3 = helper.runMigrationsAndValidate(
            name = TEST_DB,
            version = 3,
            validateDroppedTables = true,
            SubjectMigration2to3(),
        )

        val referenceIds = mutableMapOf<String, String>()
        db3.query("SELECT uuid, referenceId FROM DbBiometricTemplate").use { cursor ->
            while (cursor.moveToNext()) {
                referenceIds[cursor.getString(0)] = cursor.getString(1)
            }
        }

        // No empty referenceIds should remain
        assertThat(referenceIds.values).doesNotContain("")
        // Templates from the same subject+modality group share the same backfilled referenceId
        assertThat(referenceIds["t1"]).isEqualTo(referenceIds["t2"])
        // Templates from a different modality get a different backfilled referenceId
        assertThat(referenceIds["t3"]).isNotEqualTo(referenceIds["t1"])
        // Existing valid referenceIds are preserved
        assertThat(referenceIds["t4"]).isEqualTo("existing-ref")

        db2.close()
        db3.close()
    }

    private fun insertSubject(
        db: SupportSQLiteDatabase,
        subjectId: String,
    ) {
        db.execSQL(
            "INSERT INTO DbSubject (subjectId, projectId, attendantId, moduleId) VALUES (?, ?, ?, ?)",
            arrayOf(subjectId, "project", "attendant", "module"),
        )
    }

    private fun insertTemplate(
        db: SupportSQLiteDatabase,
        uuid: String,
        subjectId: String,
        referenceId: String,
        modality: Int,
    ) {
        db.execSQL(
            "INSERT INTO DbBiometricTemplate (uuid, subjectId, templateData, format, referenceId, modality) VALUES (?, ?, ?, ?, ?, ?)",
            arrayOf(uuid, subjectId, ByteArray(0), "format", referenceId, modality),
        )
    }

    companion object {
        private const val TEST_DB = "migration-test"
        private const val SUBJECT_1 = "subject-1"
        private const val SUBJECT_2 = "subject-2"
        private const val FINGERPRINT_MODALITY = 0
        private const val FACE_MODALITY = 1
    }
}
