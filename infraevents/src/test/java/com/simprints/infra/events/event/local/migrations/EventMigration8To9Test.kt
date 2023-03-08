package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.event.local.EventRoomDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventMigration8To9Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    fun `should remove the columns moduleIds, attendantId and modes correctly`() {
        val eventFromV4 = createEvent()
        helper.createDatabase(TEST_DB, 8).apply {
            insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, eventFromV4)
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 9, true, EventMigration8to9())

        // validate that the data was migrated properly.
        val event = MigrationTestingTools.retrieveCursorWithEventById(db, EVENT_ID)

        assertThat(event.getColumnIndex("attendantId")).isEqualTo(-1)
        assertThat(event.getColumnIndex("mode")).isEqualTo(-1)
        assertThat(event.getColumnIndex("moduleIds")).isEqualTo(-1)
    }

    private fun createEvent() = ContentValues().apply {
        put("id", EVENT_ID)
        put("type", "some-event-type")
        put("eventJson", "some-event-json")
        put("createdAt", 12345)
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
        private const val TEST_DB = "some_db"
        private const val EVENT_ID = "some-event-id"
    }
}
