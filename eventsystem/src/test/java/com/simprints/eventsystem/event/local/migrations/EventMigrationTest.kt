package com.simprints.eventsystem.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase.CONFLICT_NONE
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.local.EventRoomDatabase
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

/**
 * This test runs all the migrations where all the events have been pre-added in the database.
 * To add another migration you just need to add it in the ALL_MIGRATIONS variable. To add
 * new events create a new file in src/test/resources/all-events.
 */
@RunWith(AndroidJUnit4::class)
@Config(application = EventSystemApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    private val allEventTypes = EventType.values()

    @Test
    fun `validate all the migrations are successful`() {
        val events = loadAllEvents()
        validateAllEventsArePresent(events)

        helper.createDatabase(TEST_DB, 1).apply {
            events.forEach {
                this.insert(TABLE_NAME, CONFLICT_NONE, it)
            }
            close()
        }
        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, *ALL_MIGRATIONS)
        val cursor = db.query("SELECT * FROM $TABLE_NAME")

        while (cursor.moveToNext()) {
            val eventJson = cursor.getStringWithColumnName("eventJson")!!
            JsonHelper.fromJson(eventJson, object : TypeReference<Event>() {})
        }
        cursor.close()
    }

    private fun loadAllEvents(): List<ContentValues> =
        File("src/test/resources/all-events").walk().map {
            return@map if (it.isDirectory) null else loadEvent(it)
        }.toList().filterNotNull()

    private fun loadEvent(file: File): ContentValues =
        try {
            val eventJsonStr = file.readText()
            val eventJson = JsonHelper.fromJson<Map<String, Any>>(eventJsonStr)
            val payload = eventJson["payload"] as Map<*, *>
            ContentValues().apply {
                this.put("id", eventJson["id"] as String)
                this.put("type", eventJson["type"] as String)
                this.put("eventJson", eventJsonStr)
                this.put("createdAt", (payload["createdAt"] as Number).toLong())
                this.put("endedAt", (payload["endedAt"] as Number).toLong())
            }
        } catch (e: Exception) {
            println("Fail to parse $file")
            throw e
        }

    private fun validateAllEventsArePresent(events: List<ContentValues>) {
        allEventTypes.forEach { eventType ->
            assert(events.any { event -> event.get("type") == eventType.toString() }) {
                "missing event for $eventType"
            }
        }
    }

    companion object {
        private const val TEST_DB = "test"
        private const val TABLE_NAME = "DbEvent"
        private val ALL_MIGRATIONS = arrayOf(
            EventMigration1to2(),
            EventMigration2to3(),
            EventMigration3to4(),
            EventMigration4to5(),
            EventMigration5to6(),
            EventMigration6to7(),
            EventMigration7to8()
        )
    }
}

