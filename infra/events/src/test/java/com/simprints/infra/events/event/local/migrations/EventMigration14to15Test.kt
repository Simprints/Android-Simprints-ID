package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.events.event.local.EventRoomDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventMigration14to15Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    fun `should update events table to new structure`() {
        helper.createDatabase(TEST_DB, 14).apply {
            insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, createEvent("event-id"))
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 15, true, EventMigration14to15())
        MigrationTestingTools.retrieveCursorWithEventById(db, "event-id").use { event ->
            // Confirm correct values for new columns
            println(event.getStringWithColumnName("eventJson"))

            assertThat(event.getStringWithColumnName("scopeId")).isEqualTo("2bdc1145")
            assertThat(event.getStringWithColumnName("eventJson")).isEqualTo(NEW_EVENT)
        }
        helper.closeWhenFinished(db)
    }

    private fun createEvent(id: String) = ContentValues().apply {
        put("id", id)
        put("type", "CALLBACK_ENROLMENT")
        put("createdAt_unixMs", 123)
        put("createdAt_isTrustworthy", 0)
        put("projectId", "9WNCAbWVNrxttDe5hgwb")
        put("sessionId", "2bdc1145")
        put("eventJson", OLD_EVENT)
    }

    companion object {
        private const val TEST_DB = "migration-test"

        private val OLD_EVENT =
            """
            {"id":"d256e644-ce5b-4ec5-8909-3a372a930206","projectId":"9WNCAbWVNrxttDe5hgwb","sessionId":"2bdc1145-cbec-4e6a-ac8a-61c1e5b53bb4","payload":{"createdAt":{"ms":1706534485916,"isTrustworthy":false,"msSinceBoot":null},"eventVersion":2,"integration":"STANDARD","type":"INTENT_PARSING","endedAt":{"ms":1706534528165,"isTrustworthy":false,"msSinceBoot":null}},"type":"INTENT_PARSING"}
            """.trimIndent()

        private val NEW_EVENT =
            """
            {"id":"d256e644-ce5b-4ec5-8909-3a372a930206","projectId":"9WNCAbWVNrxttDe5hgwb","payload":{"createdAt":{"ms":1706534485916,"isTrustworthy":false,"msSinceBoot":null},"eventVersion":2,"integration":"STANDARD","type":"INTENT_PARSING","endedAt":{"ms":1706534528165,"isTrustworthy":false,"msSinceBoot":null}},"type":"INTENT_PARSING","scopeId":"2bdc1145-cbec-4e6a-ac8a-61c1e5b53bb4"}
            """.trimIndent()
    }
}
