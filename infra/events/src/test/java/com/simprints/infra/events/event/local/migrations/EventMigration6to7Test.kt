package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.*
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
class EventMigration6to7Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun `validate migration for fingerprint events`() {
        val eventId = randomUUID()
        setupV6DbWithEvent(createMatchingEvent(eventId))

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, EventMigration6to7())

        val eventJson = MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!
        val payload = JSONObject(eventJson).getJSONObject("payload")
        assertThat(payload.getString("type")).isEqualTo("ONE_TO_ONE_MATCH")
        assertThat(payload.getInt("eventVersion")).isEqualTo(2)
        assertThat(payload.getString("fingerComparisonStrategy")).isEqualTo("SAME_FINGER")
    }

    @Test
    @Throws(IOException::class)
    fun `validate  migration for face events `() {
        val eventId = randomUUID()
        setupV6DbWithEvent(createMatchingEvent(eventId, "RANK_ONE"))

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, EventMigration6to7())

        val eventJson =
            MigrationTestingTools
                .retrieveCursorWithEventById(db, eventId)
                .getStringWithColumnName("eventJson")!!

        val payload = JSONObject(eventJson).getJSONObject("payload")
        assertThat(payload.getString("type")).isEqualTo("ONE_TO_ONE_MATCH")
        assertThat(payload.getInt("eventVersion")).isEqualTo(2)
        assertThat(payload.optString("fingerComparisonStrategy")).isEmpty()
    }

    @Test
    fun `validate migration is called`() {
        val migrationSpy = spyk(EventMigration6to7())

        setupV6DbWithEvent(createMatchingEvent(randomUUID()))
        helper.runMigrationsAndValidate(TEST_DB, 7, true, migrationSpy)

        verify(exactly = 1) { migrationSpy.migrate(any()) }
    }

    @Test
    fun `validate migration query is called`() {
        val migrationSpy = spyk(EventMigration5to6())

        val db = spyk(setupV6DbWithEvent(createMatchingEvent(randomUUID()), close = false))
        migrationSpy.migrateConnectivityEvents(db)

        verify(exactly = 1) { db.query(any<SupportSQLiteQuery>()) }
    }

    private fun createMatchingEvent(
        id: String,
        matcher: String = "SIM_AFIS",
    ) = ContentValues().apply {
        this.put("id", id)
        this.put("type", "ONE_TO_ONE_MATCH")

        val eventJson =
            """      
            {
              "id": "1b4edf07-5349-418f-95ed-afe9479fa4b9",
              "type": "ONE_TO_ONE_MATCH",
              "labels": {
                "projectId": "TEST6Oai41ps1pBNrzBL",
                "sessionId": "e35c39f9-b81e-48f2-97e7-46ecc8399bb4",
                "deviceId": "f2fd8393c0a0be67"
              },
              "payload": {
                "type": "ONE_TO_ONE_MATCH",
                "eventVersion": 1,
                "createdAt": 2213412301,
                "endedAt": 4145612330,
                "candidateId": "22f3ba17-f1d8-4de2-b57a-59e5be358e00",
                "matcher": "$matcher",
                "result": {
                  "candidateId": "22f3ba17-f1d8-4de2-b57a-59e5be358e00",
                  "score": 42.24
                }
              }
            }

            """.trimIndent()
        this.put("eventJson", eventJson)
        this.put("createdAt", 1611584017198)
        this.put("endedAt", 0)
        this.put("sessionIsClosed", 0)
    }

    private fun setupV6DbWithEvent(
        eventContent: ContentValues,
        close: Boolean = true,
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 6).apply {
        this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, eventContent)
        if (close) {
            close()
        }
    }

    companion object {
        private const val TEST_DB = "test"
    }
}
