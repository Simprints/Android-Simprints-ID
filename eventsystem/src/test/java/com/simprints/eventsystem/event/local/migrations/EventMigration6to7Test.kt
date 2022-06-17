package com.simprints.eventsystem.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.truth.Truth
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.domain.models.*
import com.simprints.eventsystem.event.local.EventRoomDatabase
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.spyk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = EventSystemApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventMigration6to7Test {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun `validate migration for fingerprint events`() {
        val eventId = randomUUID()
        setupV6DbWithEvent(createMatchingEvent(eventId))

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, EventMigration6to7())

        val eventJson =
            MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
                .getStringWithColumnName("eventJson")!!
        val event = JsonHelper.fromJson(eventJson, object : TypeReference<Event>() {})

        Truth.assertThat(event).isInstanceOf(OneToOneMatchEvent::class.java)
        val oneMatchEvent = event as OneToOneMatchEvent
        Truth.assertThat(oneMatchEvent.payload.eventVersion).isEqualTo(2)
        Truth.assertThat(oneMatchEvent.payload.fingerComparisonStrategy).isEqualTo( FingerComparisonStrategy.SAME_FINGER)
    }

    @Test
    @Throws(IOException::class)
    fun `validate  migration for face events `() {
        val eventId = randomUUID()
        setupV6DbWithEvent(createMatchingEvent(eventId, "RANK_ONE"))

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, EventMigration6to7())

        val eventJson =
            MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
                .getStringWithColumnName("eventJson")!!
        val event = JsonHelper.fromJson(eventJson, object : TypeReference<Event>() {})

        Truth.assertThat(event).isInstanceOf(OneToOneMatchEvent::class.java)
        val oneMatchEvent = event as OneToOneMatchEvent
        Truth.assertThat(oneMatchEvent.payload.eventVersion).isEqualTo(2)
        Truth.assertThat(oneMatchEvent.payload.fingerComparisonStrategy).isEqualTo(null)
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

    private fun createMatchingEvent(id: String, matcher: String = "SIM_AFIS") =
        ContentValues().apply {
            this.put("id", id)
            this.put("type", "ONE_TO_ONE_MATCH")


            val eventJson = """      
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
        close: Boolean = true
    ): SupportSQLiteDatabase =
        helper.createDatabase(TEST_DB, 6).apply {
            this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, eventContent)
            if (close)
                close()
        }

    companion object {
        private const val TEST_DB = "test"
    }

}

