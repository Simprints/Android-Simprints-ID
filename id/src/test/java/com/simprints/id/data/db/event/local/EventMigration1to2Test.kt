package com.simprints.id.data.db.event.local

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_NONE
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.data.db.event.domain.models.EnrolmentEventV1
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_V1
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.domain.modality.Modes
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.extensions.getStringWithColumnName
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventMigration1to2Test {

    @get:Rule
    public val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        "${EventRoomDatabase::class.java.canonicalName}",
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        val enrolmentEventId = randomUUID()
        val openSessionCaptureEventId = randomUUID()
        val closedSessionCaptureEventId = randomUUID()

        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
        var db = helper.createDatabase(TEST_DB, 1).apply {
            val enrolmentEvent = createEnrolmentEvent(enrolmentEventId)
            val openSessionCaptureEvent = createSessionCaptureEvent(openSessionCaptureEventId, 0)
            val closedSessionCaptureEvent = createSessionCaptureEvent(closedSessionCaptureEventId, 16115)

            this.insert("DbEvent", CONFLICT_NONE, enrolmentEvent)
            this.insert("DbEvent", CONFLICT_NONE, openSessionCaptureEvent)
            this.insert("DbEvent", CONFLICT_NONE, closedSessionCaptureEvent)
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, EventMigration1to2(mockk()))

        validateEnrolmentMigration(db, enrolmentEventId)
        validateSessionCaptureMigration(db, openSessionCaptureEventId, closedSessionCaptureEventId)
    }

    private fun createEnrolmentEvent(id: String) = ContentValues().apply {
        this.put("id", id)
        this.put("type", "ENROLMENT")
        val unversionedEnrolmentEvent =
            "{\"id\":\"$id\",\"labels\":{\"projectId\":\"TEST6Oai41ps1pBNrzBL\",\"sessionId\":\"e35c39f9-b81e-48f2-97e7-46ecc8399bb4\",\"deviceId\":\"f2fd8393c0a0be67\"},\"payload\":{\"createdAt\":1611584017198,\"eventVersion\":1,\"personId\":\"61881de4-22f2-4e13-861a-21a209db8581\",\"type\":\"ENROLMENT_V1\",\"endedAt\":0},\"type\":\"ENROLMENT_V1\"}"
        this.put("eventJson", unversionedEnrolmentEvent)
        this.put("createdAt", 0)
        this.put("endedAt", 0)
    }

    private fun createSessionCaptureEvent(id: String, endedAt: Long) = ContentValues().apply {
        this.put("id", id)
        this.put("type", "SESSION_CAPTURE")
        val session = SessionCaptureEvent(
            id = id,
            projectId = "TEST6Oai41ps1pBNrzBL",
            createdAt = 1611584017198,
            modalities = listOf(Modes.FINGERPRINT),
            appVersionName = "appVersionName",
            libVersionName = "libSimprintsVersionName",
            language = "en",
            device = Device(
                "30",
                "Google_Pixel 4a",
                "deviceId"
            ),
            databaseInfo = DatabaseInfo(1)
        )

        this.put("eventJson", JsonHelper.toJson(session))
        this.put("createdAt", 1611584017198)
        this.put("endedAt", endedAt)
    }

    private fun validateEnrolmentMigration(db: SupportSQLiteDatabase, id: String) {
        val cursor = retrieveCursorWithEventById(db, id)
        assertThat(cursor.getStringWithColumnName("type")).isEqualTo(ENROLMENT_V1.toString())
        val eventJson = cursor.getStringWithColumnName("eventJson")!!
        val enrolmentEventV2 = JsonHelper.fromJson(eventJson, object : TypeReference<Event>() {})
        assertThat(enrolmentEventV2).isInstanceOf(EnrolmentEventV1::class.java)
    }

    private fun validateSessionCaptureMigration(db: SupportSQLiteDatabase, openId: String, closedId: String) {
        val openCursor = retrieveCursorWithEventById(db, openId)
        val closedCursor = retrieveCursorWithEventById(db, closedId)
        assertThat(openCursor.getStringWithColumnName("type")).isEqualTo(EventType.SESSION_CAPTURE.toString())

        val openEvent = getEventFromJson(openCursor) as SessionCaptureEvent
        assertThat(openEvent.payload.sessionIsClosed).isFalse()

        val closedEvent = getEventFromJson(closedCursor) as SessionCaptureEvent
        assertThat(closedEvent.payload.sessionIsClosed).isTrue()
    }

    private fun getEventFromJson(cursor: Cursor): Event = JsonHelper.fromJson(
        cursor.getStringWithColumnName("eventJson")!!,
        object : TypeReference<Event>() {}
    )

    private fun retrieveCursorWithEventById(db: SupportSQLiteDatabase, id: String): Cursor =
        db.query("SELECT * from DbEvent where id= ?", arrayOf(id)).apply { moveToNext() }

    companion object {
        private const val TEST_DB = "test"
    }
}
