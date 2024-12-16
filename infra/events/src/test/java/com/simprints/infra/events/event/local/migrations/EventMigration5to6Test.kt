package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.local.EventRoomDatabase
import io.mockk.spyk
import io.mockk.verify
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class EventMigration5to6Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun `validate end to end migration is successful`() {
        val eventId = randomUUID()

        setupV5DbWithEvent(eventId)

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, EventMigration5to6())

        val eventJson = MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!
            .let { JSONObject(it) }

        assertThat(eventJson.getJSONObject("payload").getInt("eventVersion")).isEqualTo(2)
        db.close()
    }

    @Test
    fun `validate migration is called`() {
        val migrationSpy = spyk(EventMigration5to6())

        setupV5DbWithEvent(randomUUID())
        helper.runMigrationsAndValidate(TEST_DB, 6, true, migrationSpy)

        verify(exactly = 1) { migrationSpy.migrate(any()) }
        verify(exactly = 1) { migrationSpy.migrateConnectivityEvents(any()) }
    }

    @Test
    fun `validate all events are migrated`() {
        val migrationSpy = spyk(EventMigration5to6())
        val eventId1 = randomUUID()
        val eventId2 = randomUUID()

        setupV5DbWithEvent(eventId1, eventId2)
        helper.runMigrationsAndValidate(TEST_DB, 6, true, migrationSpy)

        verify(exactly = 1) {
            migrationSpy.migrateConnectivityEventPayloadType(
                any(),
                any(),
                eventId1,
            )
        }
        verify(exactly = 1) {
            migrationSpy.migrateConnectivityEventPayloadType(
                any(),
                any(),
                eventId2,
            )
        }
    }

    @Test
    fun `validate migration query is called`() {
        val migrationSpy = spyk(EventMigration5to6())

        val db = spyk(setupV5DbWithEvent(randomUUID(), close = false))
        migrationSpy.migrateConnectivityEvents(db)

        verify(exactly = 1) { db.query(any<SupportSQLiteQuery>()) }
        db.close()
    }

    private fun createEvent(id: String) = ContentValues().apply {
        this.put("id", id)
        this.put("type", "CONNECTIVITY_SNAPSHOT")
        this.put(
            "eventJson",
            """
            {
                "id":"$id",
                "labels":{
                    "projectId":"TEST6Oai41ps1pBNrzBL"
                },
                "payload": {
                    "createdAt":1611584017198,
                    "eventVersion":2,
                    "connections":[
                        {"type": "WIFI","state": "CONNECTED"},
                        {"type": "MOBILE","state": "DISCONNECTED"}
                    ],
                    "type": "CONNECTIVITY_SNAPSHOT"
                },
                "type": "CONNECTIVITY_SNAPSHOT"
            }
            """.trimIndent(),
        )
        this.put("createdAt", 1611584017198)
        this.put("endedAt", 0)
        this.put("sessionIsClosed", 0)
    }

    private fun setupV5DbWithEvent(
        vararg eventId: String,
        close: Boolean = true,
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 5).apply {
        eventId.forEach {
            val event = createEvent(it)
            this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)
        }
        if (close) {
            close()
        }
    }

    companion object {
        private const val TEST_DB = "test"
    }
}
