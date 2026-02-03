package com.simprints.infra.enrolment.records.room.store.migration.com.simprints.infra.enrolment.records.room.store.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabase
import com.simprints.infra.enrolment.records.room.store.migration.SubjectMigration1to2
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class Migration1to2Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SubjectsDatabase::class.java,
    )

    @Test
    fun `when migrating from 1 to 2 then external credential table should be added`() {
        val db1 = helper.createDatabase(name = TEST_DB, version = 1)
        val db2 = helper.runMigrationsAndValidate(
            name = TEST_DB,
            version = 2,
            validateDroppedTables = true,
            SubjectMigration1to2(),
        )

        // Verify external credentials table exists
        val cursor = db2.query("SELECT name FROM sqlite_master WHERE name='DbExternalCredential'")
        Truth.assertThat(cursor.count).isEqualTo(1)
        cursor.close()
        db1.close()
        db2.close()
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
