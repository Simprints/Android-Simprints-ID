package com.simprints.eventsystem.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.EventSystemApplication
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEventV3
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3
import com.simprints.eventsystem.event.local.EventRoomDatabase
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = EventSystemApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class EventMigration7To8Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun validateMigrationForFingerprintCaptureIsSuccessful() {
        val eventId = randomUUID()

        setupV7DbWithFingerprintCaptureEvent(eventId)

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, EventMigration7to8())

        val eventJson = MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        val event = JsonHelper.fromJson(eventJson, object : TypeReference<Event>() {})
        val payloadJsonObject = JSONObject(eventJson).getJSONObject("payload")
        val fingerprintObject = payloadJsonObject.getJSONObject("fingerprint")

        assertThat(event).isInstanceOf(FingerprintCaptureEventV3::class.java)
        assertThat(event.payload.eventVersion).isEqualTo(3)
        assertThat(fingerprintObject.has("template")).isFalse()
    }

    @Test
    @Throws(IOException::class)
    fun validateMigrationForFaceCaptureIsSuccessful() {
        val eventId = randomUUID()

        setupV7DbWithFaceCaptureEvent(eventId)

        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, EventMigration7to8())

        val eventJson = MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        val event = JsonHelper.fromJson(eventJson, object : TypeReference<Event>() {})
        val payloadJsonObject = JSONObject(eventJson).getJSONObject("payload")
        val fingerprintObject = payloadJsonObject.getJSONObject("face")

        assertThat(event).isInstanceOf(FaceCaptureEventV3::class.java)
        assertThat(event.payload.eventVersion).isEqualTo(3)
        assertThat(fingerprintObject.has("template")).isFalse()
    }

    private fun setupV7DbWithFaceCaptureEvent(
        eventId: String, close: Boolean = true
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 7).apply {
        val event = createFaceCaptureEvent(eventId)
        this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)

        if (close) close()
    }

    private fun setupV7DbWithFingerprintCaptureEvent(
        eventId: String, close: Boolean = true
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 7).apply {
        val event = createFingerprintCaptureEvent(eventId)
        this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)

        if (close) close()
    }

    private fun createFingerprintCaptureEvent(id: String) = ContentValues().apply {
        put("id", id)
        put("type", OLD_FINGERPRINT_CAPTURE_EVENT)

        val event = FingerprintCaptureEvent(
            id = id, labels = EventLabels(
                projectId = SOME_PROJECT_ID
            ), payload = FingerprintCaptureEvent.FingerprintCapturePayload(
                createdAt = 1611584017198,
                eventVersion = 2,
                endedAt = 0,
                finger = IFingerIdentifier.LEFT_3RD_FINGER,
                qualityThreshold = 0,
                result = FingerprintCaptureEvent.FingerprintCapturePayload.Result.GOOD_SCAN,
                fingerprint = FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
                    finger = IFingerIdentifier.LEFT_3RD_FINGER,
                    quality = 0,
                    template = SOME_TEMPLATE
                ),
                id = id
            ), type = EventType.FINGERPRINT_CAPTURE
        )

        put("eventJson", JsonHelper.toJson(event))
        put("createdAt", 1611584017198)
        put("endedAt", 0)
        put("sessionIsClosed", 0)
    }

    private fun createFaceCaptureEvent(id: String) = ContentValues().apply {
        put("id", id)
        put("type", OLD_FACE_CAPTURE_EVENT)

        val event = FaceCaptureEvent(
            id = id, labels = EventLabels(
                projectId = SOME_PROJECT_ID
            ), payload = FaceCaptureEvent.FaceCapturePayload(
                id = id,
                createdAt = 1611584017198,
                endedAt = 0,
                eventVersion = 2,
                attemptNb = 0,
                qualityThreshold = 0.0f,
                result = FaceCaptureEvent.FaceCapturePayload.Result.VALID,
                isFallback = false,
                face = FaceCaptureEvent.FaceCapturePayload.Face(
                    yaw = 0.0f,
                    roll = 0.0f,
                    quality = 0.0f,
                    template = SOME_FACE_TEMPLATE
                )
            ), type = EventType.FACE_CAPTURE
        )

        put("eventJson", JsonHelper.toJson(event))
        put("createdAt", 1611584017198)
        put("endedAt", 0)
        put("sessionIsClosed", 0)
    }

    companion object {
        private const val SOME_TEMPLATE = "some_template"
        private const val TEST_DB = "some_db"
        private const val SOME_PROJECT_ID = "some_project_id"
        private const val OLD_FACE_CAPTURE_EVENT = "FACE_CAPTURE"
        private const val SOME_FACE_TEMPLATE = "some_face_template"
        private const val OLD_FINGERPRINT_CAPTURE_EVENT = "FINGERPRINT_CAPTURE"
    }
}

