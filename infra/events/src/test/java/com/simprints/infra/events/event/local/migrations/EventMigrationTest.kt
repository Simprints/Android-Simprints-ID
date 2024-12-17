package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase.CONFLICT_NONE
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameDeserializer
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameSerializer
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.local.EventRoomDatabase
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * This test runs all the migrations where all the events have been pre-added in the database.
 * To add another migration you just need to add it in the ALL_MIGRATIONS variable. To add
 * new events create a new file in src/test/resources/all-events.
 */
@RunWith(AndroidJUnit4::class)
class EventMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
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
        val db = helper.runMigrationsAndValidate(TEST_DB, 14, true, *ALL_MIGRATIONS)
        db.query("SELECT * FROM $TABLE_NAME").use { cursor ->
            while (cursor.moveToNext()) {
                val eventJson = cursor.getStringWithColumnName("eventJson")!!
                JsonHelper.fromJson(
                    json = eventJson,
                    type = object : TypeReference<Event>() {},
                    module = tokenizeSerializationModule,
                )
            }
        }
        helper.closeWhenFinished(db)
    }

    private fun loadAllEvents(): List<ContentValues> = File("src/test/resources/all-events")
        .walk()
        .map {
            return@map if (it.isDirectory) null else loadEvent(it)
        }.toList()
        .filterNotNull()

    private fun loadEvent(file: File): ContentValues = try {
        // Some migrations expect that eventJson is a string without spaces or new lines as it is in the real database
        val eventJsonStr = file.readText().replace("\n", "").replace(" ", "")
        val eventJson = JSONObject(eventJsonStr)
        val payload = eventJson.getJSONObject("payload")

        ContentValues().apply {
            this.put("id", eventJson.getString("id"))
            this.put("type", eventJson.getString("type"))
            this.put("eventJson", eventJsonStr)
            this.put("createdAt", payload.getLong("createdAt"))
            this.put("endedAt", payload.getLong("endedAt"))
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
            EventMigration7to8(),
            EventMigration8to9(),
            EventMigration9to10(),
            EventMigration10to11(),
            EventMigration11to12(),
            EventMigration12to13(),
            EventMigration13to14(),
            EventMigration14to15(),
        )
        val tokenizeSerializationModule = SimpleModule().apply {
            addSerializer(TokenizableString::class.java, TokenizationClassNameSerializer())
            addDeserializer(TokenizableString::class.java, TokenizationClassNameDeserializer())
        }
    }
}
