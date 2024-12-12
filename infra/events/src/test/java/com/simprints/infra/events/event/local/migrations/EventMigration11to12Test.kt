package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getIntWithColumnName
import com.simprints.core.tools.extentions.getLongWithColumnName
import com.simprints.infra.events.event.local.EventRoomDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventMigration11to12Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    fun `Should replace createdAt with timestamp structs in session scope`() {
        helper.createDatabase(TEST_DB, 11).apply {
            insert("DbSessionScope", SQLiteDatabase.CONFLICT_NONE, createSessionScope("session-id"))
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, EventMigration11to12())
        MigrationTestingTools.retrieveCursorWithSessionScopeById(db, "session-id").use { scope ->
            assertThat(scope.getLongWithColumnName("start_unixMs")).isEqualTo(12)
            assertThat(scope.getIntWithColumnName("start_isTrustworthy")).isEqualTo(0)

            // Also check that end timestamp has been updated with all null
            assertThat(scope.getLongWithColumnName("end_unixMs")).isNull()
            assertThat(scope.getIntWithColumnName("end_isTrustworthy")).isNull()
        }
    }

    @Test
    fun `Should replace ended with timestamp structs in session scope`() {
        helper.createDatabase(TEST_DB, 11).apply {
            insert(
                "DbSessionScope",
                SQLiteDatabase.CONFLICT_NONE,
                createSessionScope("session-id", 34),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 12, true, EventMigration11to12())
        MigrationTestingTools.retrieveCursorWithSessionScopeById(db, "session-id").use { scope ->
            assertThat(scope.getLongWithColumnName("end_unixMs")).isEqualTo(34)
            assertThat(scope.getIntWithColumnName("end_isTrustworthy")).isEqualTo(0)
        }
    }

    private fun createSessionScope(
        id: String,
        ended: Long? = null,
    ) = ContentValues().apply {
        put("id", id)
        put("projectId", "some-project-id")
        put("createdAt", 12)
        ended?.let { put("endedAt", it) }
        put("payloadJson", "{}")
    }

    companion object {
        private const val TEST_DB = "some_db"
    }
}
