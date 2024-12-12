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
class EventMigration15to16Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    fun `should rename FACE_LICENSE_MISSING and FACE_LICENSE_INVALID to LICENSE_MISSING and LICENSE_INVALID`() {
        val missingLicenseEventId = "event-id-missing"
        val eventWithFaceLicenseMissing =
            createEvent(missingLicenseEventId, "FACE_LICENSE_MISSING")
        val invalidLicenseEventId = "event-id-invalid"
        val eventWithFaceLicenseInvalid =
            createEvent(invalidLicenseEventId, "FACE_LICENSE_INVALID")

        helper.createDatabase(EventMigration15to16.TABLE_NAME, 15).apply {
            insert(
                EventMigration15to16.TABLE_NAME,
                SQLiteDatabase.CONFLICT_NONE,
                eventWithFaceLicenseMissing,
            )
            insert(
                EventMigration15to16.TABLE_NAME,
                SQLiteDatabase.CONFLICT_NONE,
                eventWithFaceLicenseInvalid,
            )
            close()
        }
        val db = helper.runMigrationsAndValidate(
            EventMigration15to16.TABLE_NAME,
            16,
            true,
            EventMigration15to16(),
        )
        MigrationTestingTools.retrieveCursorWithEventById(db, missingLicenseEventId).use { event ->
            // the event json doesn't contain FACE_LICENSE_MISSING
            assertThat(event.getStringWithColumnName("eventJson")).contains(
                "\"type\":\"LICENSE_MISSING\"",
            )
        }
        MigrationTestingTools.retrieveCursorWithEventById(db, invalidLicenseEventId).use { event ->
            // the event json doesn't contain FACE_LICENSE_INVALID
            assertThat(event.getStringWithColumnName("eventJson")).contains(
                "\"type\":\"LICENSE_INVALID\"",
            )
        }
        helper.closeWhenFinished(db)
    }

    @Test
    fun `should not rename FACE_LICENSE_MISSING and FACE_LICENSE_INVALID to LICENSE_MISSING and LICENSE_INVALID if they are not present`() {
        val eventWithDifferentError = createEvent("event-id-different", "DIFFERENT_ERROR")

        helper.createDatabase(EventMigration15to16.TABLE_NAME, 15).apply {
            insert(
                EventMigration15to16.TABLE_NAME,
                SQLiteDatabase.CONFLICT_NONE,
                eventWithDifferentError,
            )
            close()
        }
        val db = helper.runMigrationsAndValidate(
            EventMigration15to16.TABLE_NAME,
            16,
            true,
            EventMigration15to16(),
        )
        MigrationTestingTools.retrieveCursorWithEventById(db, "event-id-different").use { event ->
            // the event json doesn't contain FACE_LICENSE_MISSING or FACE_LICENSE_INVALID
            assertThat(event.getStringWithColumnName("eventJson")).contains(
                "\"type\":\"DIFFERENT_ERROR\"",
            )
        }
        helper.closeWhenFinished(db)
    }

    private fun createEvent(
        id: String,
        errorType: String,
    ): ContentValues = ContentValues().apply {
        put("id", id)
        put("createdAt_unixMs", 0)
        put("createdAt_isTrustworthy", 0)
        put("type", "ALERT_SCREEN")
        put("eventJson", "{\"type\":\"$errorType\"}")
    }
}
