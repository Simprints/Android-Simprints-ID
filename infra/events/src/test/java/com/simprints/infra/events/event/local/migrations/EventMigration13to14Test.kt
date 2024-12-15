package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getIntWithColumnName
import com.simprints.core.tools.extentions.getLongWithColumnName
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.event.local.EventRoomDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventMigration13to14Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    fun `Should rename the session scope table to event scope table`() {
        helper.createDatabase(TEST_DB, 13).apply {
            insert("DbSessionScope", SQLiteDatabase.CONFLICT_NONE, createSessionScope("session-id"))
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 14, true, EventMigration13to14())

        MigrationTestingTools.retrieveCursorWithEventScopeById(db, "session-id").use { scope ->
            assertThat(scope.getStringWithColumnName("type")).isEqualTo(EventScopeType.SESSION.name)
            assertThat(scope.getLongWithColumnName("start_unixMs")).isEqualTo(12)
            assertThat(scope.getIntWithColumnName("start_isTrustworthy")).isEqualTo(0)
            assertThat(scope.getLongWithColumnName("end_unixMs")).isEqualTo(34)
            assertThat(scope.getIntWithColumnName("end_isTrustworthy")).isEqualTo(1)
            assertThat(scope.getLongWithColumnName("end_msSinceBoot")).isEqualTo(56)
        }
        helper.closeWhenFinished(db)
    }

    private fun createSessionScope(
        id: String,
        ended: Long? = null,
    ) = ContentValues().apply {
        put("id", id)
        put("projectId", "some-project-id")
        put("start_unixMs", 12)
        put("start_isTrustworthy", 0)
        put("end_unixMs", 34)
        put("end_isTrustworthy", 1)
        put("end_msSinceBoot", 56)
        ended?.let { put("endedAt", it) }
        put("payloadJson", "{}")
    }

    companion object {
        private const val TEST_DB = "some_db"
    }
}
