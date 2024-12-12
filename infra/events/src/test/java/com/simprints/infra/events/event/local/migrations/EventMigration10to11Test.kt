package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getLongWithColumnName
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.DatabaseInfo
import com.simprints.infra.events.event.domain.models.scope.Device
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopePayload
import com.simprints.infra.events.event.domain.models.scope.Location
import com.simprints.infra.events.event.local.EventRoomDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventMigration10to11Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    fun `Should convert session capture events to session scope`() {
        val ongoingSessionId = randomUUID()
        val endedSessionId = randomUUID()
        createV10DatabaseWithEvents(
            listOf(
                createCaptureSessionEvent(randomUUID(), ongoingSessionId),
                createCaptureSessionEvent(randomUUID(), endedSessionId, true),
            ),
        )

        val db = helper.runMigrationsAndValidate(TEST_DB, 11, true, EventMigration10to11())

        getSessionScopeCursor(db, ongoingSessionId).use {
            verifyScope(it, ongoingSessionId)
        }
        getSessionScopeCursor(db, endedSessionId).use {
            verifyScope(it, endedSessionId, true)
        }

        helper.closeWhenFinished(db)
    }

    @Test
    fun `Should delete correct events after migration`() {
        createV10DatabaseWithEvents(
            listOf(
                createCaptureSessionEvent(randomUUID(), randomUUID()),
                createOtherEvent(randomUUID(), randomUUID()),
                createTerminationEvent(randomUUID(), randomUUID()),
            ),
        )
        val db = helper.runMigrationsAndValidate(TEST_DB, 11, true, EventMigration10to11())

        db
            .query(
                "SELECT * FROM DbEvent WHERE type = ?",
                arrayOf(OLD_CAPTURE_SESSION_EVENT_KEY),
            ).use { assertThat(it.count).isEqualTo(0) }

        db
            .query(
                "SELECT * FROM DbEvent WHERE type = ?",
                arrayOf(OLD_ARTIFICIAL_TERMINATION_EVENT_KEY),
            ).use { assertThat(it.count).isEqualTo(0) }

        db.query("SELECT * FROM DbEvent").use { assertThat(it.count).isEqualTo(1) }

        helper.closeWhenFinished(db)
    }

    private fun createV10DatabaseWithEvents(events: List<ContentValues>) {
        helper.createDatabase(TEST_DB, 10).apply {
            events.forEach { insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, it) }
            close()
        }
    }

    private fun getSessionScopeCursor(
        db: SupportSQLiteDatabase,
        sessionId: String,
    ) = db
        .query("SELECT * from DbSessionScope where id= ?", arrayOf(sessionId))
        .apply { moveToNext() }

    private fun createCaptureSessionEvent(
        id: String,
        sessionId: String,
        ended: Boolean = false,
    ) = ContentValues().apply {
        val event =
            """
            {
            "id":"$id",
            "type":"$OLD_CAPTURE_SESSION_EVENT_KEY",
            "labels":{
                "projectId":"$PROJECT_ID",
                "sessionId": "$sessionId",
                "deviceId": "$DEVICE_ID"
            },
            "payload":{
                "eventVersion":2,
                "id":"$id",
                "projectId":"$PROJECT_ID",
                "type":"$OLD_CAPTURE_SESSION_EVENT_KEY",
                "createdAt":$CREATED_AT,
                "endedAt":${ENDED_AT.takeIf { ended } ?: 0},
                "modalities":["FINGERPRINT","FACE"],
                "appVersionName":"1.0.0",
                "libVersionName":"1.0.0",
                "language":"en",
                "device":{
                    "androidSdkVersion":"29",
                    "deviceModel":"Pixel 3",
                    "deviceId":"$DEVICE_ID"
                },
                "databaseInfo":{
                    "sessionCount":4,
                    "recordCount":10
                },
                "location":{
                    "latitude":42.0,
                    "longitude":42.0
                },
                "sessionIsClosed":$ended
            }
            }
            """.trimIndent()

        this.put("id", id)
        this.put("type", OLD_CAPTURE_SESSION_EVENT_KEY)
        this.put("eventJson", event)
        this.put("createdAt", CREATED_AT)
        this.put("endedAt", ENDED_AT.takeIf { ended } ?: 0)
        this.put("sessionIsClosed", 0)
    }

    private fun createTerminationEvent(
        id: String,
        sessionId: String,
    ) = ContentValues().apply {
        val event =
            """
            {
            "id":"$id",
            "type":"$OLD_ARTIFICIAL_TERMINATION_EVENT_KEY",
            "labels":{
                "projectId":"$PROJECT_ID",
                "sessionId": "$sessionId",
                "deviceId": "$DEVICE_ID"
            },
            "payload":{
                "type":"$OLD_ARTIFICIAL_TERMINATION_EVENT_KEY",
                "eventVersion":2,
                "createdAt":$CREATED_AT,
                "endedAt":$ENDED_AT,
                "reason":"NEW_SESSION"
            }
            }
            """.trimIndent()

        this.put("id", id)
        this.put("type", OLD_ARTIFICIAL_TERMINATION_EVENT_KEY)
        this.put("eventJson", event)
        this.put("createdAt", CREATED_AT)
        this.put("endedAt", ENDED_AT)
        this.put("sessionIsClosed", 0)
    }

    private fun createOtherEvent(
        id: String,
        sessionId: String,
    ) = ContentValues().apply {
        val event =
            """
            {
            "id":"$id",
            "type":"${EventType.REFUSAL_KEY}",
            "labels":{
                "projectId":"$PROJECT_ID",
                "sessionId": "$sessionId",
                "deviceId": "$DEVICE_ID"
            },
            "payload":{
                "type":"${EventType.REFUSAL_KEY}",
                "eventVersion":2,
                "createdAt":$CREATED_AT,
                "endedAt":$ENDED_AT,
                "reason":"REFUSED_RELIGION",
                "otherText":"AAA"
            }
            }
            """.trimIndent()

        this.put("id", id)
        this.put("type", EventType.REFUSAL_KEY)
        this.put("eventJson", event)
        this.put("createdAt", CREATED_AT)
        this.put("endedAt", ENDED_AT)
        this.put("sessionIsClosed", 0)
    }

    private fun verifyScope(
        scopeCursor: Cursor,
        sessionId: String,
        shouldHaveEnded: Boolean = false,
    ) {
        val scopePayload = JsonHelper.fromJson(
            scopeCursor.getStringWithColumnName("payloadJson").orEmpty(),
            object : TypeReference<EventScopePayload>() {},
        )

        assertThat(scopeCursor.getStringWithColumnName("id")).isEqualTo(sessionId)
        assertThat(scopeCursor.getStringWithColumnName("projectId")).isEqualTo(PROJECT_ID)
        assertThat(scopeCursor.getLongWithColumnName("createdAt")).isEqualTo(CREATED_AT)

        assertThat(scopePayload.language).isEqualTo("en")
        assertThat(scopePayload.sidVersion).isEqualTo("1.0.0")
        assertThat(scopePayload.modalities).isEqualTo(
            listOf(
                GeneralConfiguration.Modality.FINGERPRINT,
                GeneralConfiguration.Modality.FACE,
            ),
        )
        assertThat(scopePayload.device).isEqualTo(Device("29", "Pixel 3", DEVICE_ID))
        assertThat(scopePayload.databaseInfo).isEqualTo(DatabaseInfo(4, 10))
        assertThat(scopePayload.location).isEqualTo(Location(42.0, 42.0))

        if (shouldHaveEnded) {
            assertThat(scopeCursor.getLongWithColumnName("endedAt")).isEqualTo(ENDED_AT)
            assertThat(scopePayload.endCause).isEqualTo(EventScopeEndCause.NEW_SESSION)
        } else {
            assertThat(scopeCursor.getLongWithColumnName("endedAt")).isNull()
            assertThat(scopePayload.endCause).isNull()
        }
    }

    companion object {
        private const val TEST_DB = "some_db"

        private const val OLD_CAPTURE_SESSION_EVENT_KEY = "SESSION_CAPTURE"
        private const val OLD_ARTIFICIAL_TERMINATION_EVENT_KEY = "ARTIFICIAL_TERMINATION"

        private const val PROJECT_ID = "aProjectID"
        private const val DEVICE_ID = "aDeviceID"
        private const val CREATED_AT = 123
        private const val ENDED_AT = 123
    }
}
