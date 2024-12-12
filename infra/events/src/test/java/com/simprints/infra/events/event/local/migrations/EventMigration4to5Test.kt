package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getIntWithColumnName
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.events.event.local.EventRoomDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class EventMigration4to5Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        val version4Event = createEvent()
        // create db with schema version 4.
        helper.createDatabase(TEST_DB, 4).apply {
            // insert some data using SQL queries.
            this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, version4Event)
            // close db, as its prepared for the next version.
            close()
        }

        // re-open the database with version 5 and initiate MIGRATION 4 to 5
        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            5,
            true,
            EventMigration4to5(),
        )

        // validate that the data was migrated properly.
        val event = MigrationTestingTools.retrieveCursorWithEventById(db, EVENT_ID)
        for (key in version4Event.keySet()) {
            if (key == "subjectId") continue

            val field = version4Event[key]
            val dbValue = when (field) {
                is Int -> event.getIntWithColumnName(key)
                else -> event.getStringWithColumnName(key)
            }

            assertThat(dbValue).isEqualTo(field)
        }
    }

    private fun createEvent() = ContentValues().apply {
        put("id", EVENT_ID)
        put("type", "some-event-type")
        put("eventJson", "some-event-json")
        put("createdAt", 12345)
        put("subjectId", "some-event-subjectId")
        put("endedAt", 12445)
        put("sessionIsClosed", 0)
        put("projectId", "some-event-projectId")
        put("attendantId", "some-event-attendantId")
        put("moduleIds", "some-event-moduleIds")
        put("mode", "some-event-mode")
        put("sessionId", "some-event-sessionId")
        put("deviceId", "some-event-deviceId")
    }

    companion object {
        private const val TEST_DB = "test"
        private const val EVENT_ID = "some-event-id"
    }
}
