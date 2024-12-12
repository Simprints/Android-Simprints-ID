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
import com.simprints.infra.events.event.local.EventRoomDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventMigration12to13Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    fun `should update events table to new structure`() {
        helper.createDatabase(TEST_DB, 12).apply {
            insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, createEvent("event-id"))
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 13, true, EventMigration12to13())
        MigrationTestingTools.retrieveCursorWithEventById(db, "event-id").use { event ->
            // Confirm correct values for new columns
            assertThat(event.getLongWithColumnName("createdAt_unixMs")).isEqualTo(123)
            assertThat(event.getIntWithColumnName("createdAt_isTrustworthy")).isEqualTo(0)
            assertThat(event.getLongWithColumnName("createdAt_msSinceBoot")).isNull()
            assertThat(event.getStringWithColumnName("eventJson")).isEqualTo(NEW_EVENT)
        }
        helper.closeWhenFinished(db)
    }

    @Test
    fun `should correctly convert event structure`() {
        assertThat(EventMigration12to13().convertEventJson(OLD_EVENT)).isEqualTo(NEW_EVENT)
    }

    private fun createEvent(id: String) = ContentValues().apply {
        this.put("id", id)
        this.put("type", "CALLBACK_ENROLMENT")
        this.put("eventJson", OLD_EVENT)
        this.put("createdAt", 123)
        this.put("endedAt", 345)
        this.put("sessionIsClosed", 0)
    }

    companion object {
        private const val TEST_DB = "migration-test"

        private val OLD_EVENT =
            """
            {"id":"d256e644-ce5b-4ec5-8909-3a372a930206","labels":{"projectId":"9WNCAbWVNrxttDe5hgwb","sessionId":"2bdc1145-cbec-4e6a-ac8a-61c1e5b53bb4","deviceId":"d294a268b0d54f58"},"payload":{"createdAt":1706534485916,"eventVersion":1,"integration":"STANDARD","type":"INTENT_PARSING","endedAt":0,"endedAt":1706534528165},"type":"INTENT_PARSING"}
            """.trimIndent()

        private val NEW_EVENT =
            """
            {"id":"d256e644-ce5b-4ec5-8909-3a372a930206","projectId":"9WNCAbWVNrxttDe5hgwb","sessionId":"2bdc1145-cbec-4e6a-ac8a-61c1e5b53bb4","payload":{"createdAt":{"ms":1706534485916,"isTrustworthy":false,"msSinceBoot":null},"eventVersion":2,"integration":"STANDARD","type":"INTENT_PARSING","endedAt":null,"endedAt":{"ms":1706534528165,"isTrustworthy":false,"msSinceBoot":null}},"type":"INTENT_PARSING"}
            """.trimIndent()
    }
}
