package com.simprints.eventsystem.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getIntWithColumnName
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.local.EventRoomDatabase
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = EventSystemApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventMigration4to5Test {

    private val TEST_DB = "test"

    // Array of all migrations
    private val ALL_MIGRATIONS = arrayOf(
        EventMigration1to2(),
        EventMigration2to3(),
        EventMigration3to4(),
        EventMigration4to5(),
        EventMigration5to6(),
        EventMigration6to7(),
        EventMigration7to8(),
        EventMigration8to9()
    )

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
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
            TEST_DB, 5, true, EventMigration4to5()
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

    @Test
    @Throws(IOException::class)
    fun runAllMigrations() {
        // create earliest version of the database.
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }

        // open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            EventRoomDatabase::class.java, TEST_DB
        ).addMigrations(*ALL_MIGRATIONS).build().apply {
            // retrieve the database reference to force-open the db
            // instance after migrations have been run
            openHelper.writableDatabase
            // then close the db
            close()
        }
    }

    private fun
        createEvent() = ContentValues().apply {
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
        private const val EVENT_ID = "some-event-id"
    }

}
