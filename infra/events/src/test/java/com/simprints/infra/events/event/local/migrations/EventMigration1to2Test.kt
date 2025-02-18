package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase.CONFLICT_NONE
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.local.EventRoomDatabase
import com.simprints.infra.events.event.local.migrations.MigrationTestingTools.retrieveCursorWithEventById
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import dagger.hilt.android.testing.HiltTestApplication
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventMigration1to2Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        val enrolmentEventId = randomUUID()
        val openSessionCaptureEventId = randomUUID()
        val closedSessionCaptureEventId = randomUUID()

        val enrolmentEvent = createEnrolmentEvent(enrolmentEventId)
        val openSessionCaptureEvent = createSessionCaptureEvent(openSessionCaptureEventId, 0)
        val closedSessionCaptureEvent = createSessionCaptureEvent(closedSessionCaptureEventId, 2)

        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
        var db = helper.createDatabase(TEST_DB, 1).apply {
            insert("DbEvent", CONFLICT_NONE, enrolmentEvent)
            insert("DbEvent", CONFLICT_NONE, openSessionCaptureEvent)
            insert("DbEvent", CONFLICT_NONE, closedSessionCaptureEvent)
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, EventMigration1to2())

        validateEnrolmentMigration(db, enrolmentEventId)
        validateSessionCaptureMigration(db, openSessionCaptureEventId, closedSessionCaptureEventId)

        helper.closeWhenFinished(db)
    }

    private fun createEnrolmentEvent(id: String) = ContentValues().apply {
        this.put("id", id)
        this.put("type", "ENROLMENT")
        val unversionedEnrolmentEvent =
            """{"id":"$id","labels":{"projectId":"TEST6Oai41ps1pBNrzBL","sessionId":"e35c39f9-b81e-48f2-97e7-46ecc8399bb4","deviceId":"f2fd8393c0a0be67"},"payload":{"createdAt":1611584017198,"eventVersion":1,"personId":"61881de4-22f2-4e13-861a-21a209db8581","type":"ENROLMENT_V1","endedAt":0},"type":"ENROLMENT_V1"}"""
        this.put("eventJson", unversionedEnrolmentEvent)
        this.put("createdAt", 0)
        this.put("endedAt", 0)
    }

    private fun createSessionCaptureEvent(
        id: String,
        endedAt: Long,
    ) = ContentValues().apply {
        this.put("id", id)
        this.put("type", "SESSION_CAPTURE")
        this.put(
            "eventJson",
            """
            {
                "id": "$id",
                "labels": {
                    "projectId": "TEST6Oai41ps1pBNrzBL",
                    "sessionId": "e35c39f9-b81e-48f2-97e7-46ecc8399bb4",
                    "deviceId": "f2fd8393c0a0be67"
                },
                "payload": {
                    "id": "e35c39f9-b81e-48f2-97e7-46ecc8399bb4",
                    "createdAt": 1611584017198,
                    "modalities": ["FINGERPRINT"],
                    "appVersionName": "appVersionName",
                    "libVersionName": "libSimprintsVersionName",
                    "language": "en",
                    "device": {
                        "sdkVersion": "30",
                        "model": "Google_Pixel 4a",
                        "deviceId": "deviceId"
                    },
                    "databaseInfo": {"version": 1}
                },
                "type": "SESSION_CAPTURE"
            }
            """.trimIndent(),
        )
        this.put("createdAt", 1611584017198)
        this.put("endedAt", endedAt)
    }

    private fun validateEnrolmentMigration(
        db: SupportSQLiteDatabase,
        id: String,
    ) {
        val cursor = retrieveCursorWithEventById(db, id)
        assertThat(cursor.getStringWithColumnName("type")).isEqualTo("ENROLMENT_V1")

        val eventJson = JSONObject(cursor.getStringWithColumnName("eventJson")!!)
        assertThat(eventJson.getString("type")).isEqualTo("ENROLMENT_V1")
    }

    private fun validateSessionCaptureMigration(
        db: SupportSQLiteDatabase,
        openId: String,
        closedId: String,
    ) {
        retrieveCursorWithEventById(db, openId).use {
            assertThat(it.getStringWithColumnName("type")).isEqualTo("SESSION_CAPTURE")

            val eventJson = JSONObject(it.getStringWithColumnName("eventJson")!!)
            assertThat(eventJson.getJSONObject("payload").optBoolean("sessionIsClosed")).isFalse()
        }

        retrieveCursorWithEventById(db, closedId).use {
            assertThat(it.getStringWithColumnName("type")).isEqualTo("SESSION_CAPTURE")

            val eventJson = JSONObject(it.getStringWithColumnName("eventJson")!!)
            assertThat(eventJson.getJSONObject("payload").optBoolean("sessionIsClosed")).isTrue()
        }
    }

    companion object {
        private const val TEST_DB = "test"
    }
}
