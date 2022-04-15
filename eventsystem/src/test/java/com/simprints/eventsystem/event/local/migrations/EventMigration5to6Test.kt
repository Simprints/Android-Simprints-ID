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
import com.simprints.core.tools.utils.SimNetworkUtils.Connection
import com.simprints.core.tools.utils.SimNetworkUtils.ConnectionState.CONNECTED
import com.simprints.core.tools.utils.SimNetworkUtils.ConnectionType
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.domain.models.ConnectivitySnapshotEvent
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
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
class EventMigration5to6Test {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun `validate end to end migration is successful`() {
        val eventId = randomUUID()

        setupV5DbWithEvent(eventId)

        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, EventMigration5to6())

        val eventJson =
            MigrationTestingTools.retrieveCursorWithEventById(db, eventId).getStringWithColumnName("eventJson")!!
        val event = JsonHelper.fromJson(eventJson, object : TypeReference<Event>() {})

        Truth.assertThat(event).isInstanceOf(ConnectivitySnapshotEvent::class.java)
        assert(event.payload.eventVersion == 2)
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
            migrationSpy.migrateEnrolmentEventPayloadType(
                any(),
                any(),
                eventId1
            )
        }
        verify(exactly = 1) {
            migrationSpy.migrateEnrolmentEventPayloadType(
                any(),
                any(),
                eventId2
            )
        }
    }

    @Test
    fun `validate migration query is called`() {
        val migrationSpy = spyk(EventMigration5to6())

        val db = spyk(setupV5DbWithEvent(randomUUID(), close = false))
        migrationSpy.migrateConnectivityEvents(db)

        verify(exactly = 1) { db.query(any<SupportSQLiteQuery>()) }
    }

    private fun createEvent(id: String) = ContentValues().apply {
        this.put("id", id)
        this.put("type", "CONNECTIVITY_SNAPSHOT")
        val event = ConnectivitySnapshotEvent(
            id = id,
            labels = EventLabels(
                projectId = "TEST6Oai41ps1pBNrzBL"
            ),
            ConnectivitySnapshotEvent.ConnectivitySnapshotPayload(
                1611584017198,
                2,
                listOf(Connection(ConnectionType.WIFI, CONNECTED)),
                EventType.CONNECTIVITY_SNAPSHOT,
                0
            ),
            EventType.CONNECTIVITY_SNAPSHOT
        )
        this.put("eventJson", JsonHelper.toJson(event))
        this.put("createdAt", 1611584017198)
        this.put("endedAt", 0)
        this.put("sessionIsClosed", 0)
    }

    private fun setupV5DbWithEvent(
        vararg eventId: String,
        close: Boolean = true
    ): SupportSQLiteDatabase =
        helper.createDatabase(TEST_DB, 5).apply {
            eventId.forEach {
                val event = createEvent(it)
                this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)
            }
            if (close)
                close()
        }

    companion object {
        private const val TEST_DB = "test"
    }

}

