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
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEventV3
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3
import com.simprints.eventsystem.event.local.EventRoomDatabase
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

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

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

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val faceCaptureEventJson = MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        val faceCaptureBiometricsEventJson = db.query(
            "SELECT * FROM DbEvent WHERE type = ?", arrayOf("FACE_CAPTURE_BIOMETRICS")
        ).apply { moveToNext() }.getStringWithColumnName("eventJson")!!

        val faceCaptureEvent =
            JsonHelper.fromJson(faceCaptureEventJson, object : TypeReference<Event>() {})
        val faceCaptureBiometricsEvent =
            JsonHelper.fromJson(faceCaptureBiometricsEventJson, object : TypeReference<Event>() {})

        val fingerprintCapturePayload = JSONObject(faceCaptureEventJson).getJSONObject("payload")
        val fingerprintCaptureFace = fingerprintCapturePayload.getJSONObject("face")
        val fingerprintCaptureBiometricsPayload =
            JSONObject(faceCaptureBiometricsEventJson).getJSONObject("payload")
        val fingerprintCaptureBiometricsFace =
            fingerprintCaptureBiometricsPayload.getJSONObject("face")

        assertThat(faceCaptureEvent).isInstanceOf(FaceCaptureEventV3::class.java)
        assertThat(faceCaptureEvent.payload.eventVersion).isEqualTo(3)
        assertThat(fingerprintCaptureFace.has("template")).isFalse()

        assertThat(faceCaptureBiometricsEvent).isInstanceOf(FaceCaptureBiometricsEvent::class.java)
        assertThat(faceCaptureBiometricsEvent.payload.eventVersion).isEqualTo(0)
        assertThat(fingerprintCaptureBiometricsFace.has("template")).isTrue()
        assertThat((faceCaptureBiometricsEvent as FaceCaptureBiometricsEvent).payload.face.template).isEqualTo(
            "some_face_template"
        )
        assertThat(faceCaptureBiometricsEvent.payload.id).isEqualTo(
            (faceCaptureEvent as FaceCaptureEventV3).payload.id
        )
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
        this.put("id", id)
        this.put("type", OLD_FINGERPRINT_CAPTURE_EVENT)

        val event =
            """
                {
                "id":"2022ae95-d4c0-469c-85d6-750659598bbd",
                "labels":{
                    "projectId":"some_project_id"
                },
                "payload":{
                    "createdAt":1611584017198,
                    "eventVersion":2,
                    "endedAt":0,
                    "finger":"LEFT_3RD_FINGER",
                    "qualityThreshold":0,
                    "result":"GOOD_SCAN",
                    "fingerprint":{
                        "finger":"LEFT_3RD_FINGER",
                        "quality":0,
                        "template":"some_template",
                        "format":"ISO_19794_2"
                        },
                "id":"2022ae95-d4c0-469c-85d6-750659598bbd",
                "type":"FINGERPRINT_CAPTURE"
                        },
                "type":"FINGERPRINT_CAPTURE"}
                """
                .trimIndent()

        this.put("eventJson", event)
        this.put("createdAt", 1611584017198)
        this.put("endedAt", 0)
        this.put("sessionIsClosed", 0)
    }

    private fun createFaceCaptureEvent(id: String) = ContentValues().apply {
        put("id", id)
        put("type", OLD_FACE_CAPTURE_EVENT)

        val event =
            """
                {
                "id":"977f54f6-a1b8-46d0-a1f4-d1e0685926b9",
                "labels":{
                    "projectId":"some_project_id"
                },
                "payload":{
                    "id":"977f54f6-a1b8-46d0-a1f4-d1e0685926b9",
                    "createdAt":1611584017198,
                    "endedAt":0,
                    "eventVersion":2,
                    "attemptNb":0,
                    "qualityThreshold":0.0,
                    "result":"VALID",
                    "isFallback":false,
                    "face":{
                        "yaw":0.0,
                        "roll":0.0,
                        "quality":0.0,
                        "template":"some_face_template",
                        "format":"RANK_ONE_1_23"
                        },
                    "type":"FACE_CAPTURE"
                },
                "type":"FACE_CAPTURE"
                }"""
                .trimIndent()

        put("eventJson", event)
        put("createdAt", 1611584017198)
        put("endedAt", 0)
        put("sessionIsClosed", 0)
    }

    companion object {
        private const val TEST_DB = "some_db"
        private const val OLD_FACE_CAPTURE_EVENT = "FACE_CAPTURE"
        private const val OLD_FINGERPRINT_CAPTURE_EVENT = "FINGERPRINT_CAPTURE"
    }
}

