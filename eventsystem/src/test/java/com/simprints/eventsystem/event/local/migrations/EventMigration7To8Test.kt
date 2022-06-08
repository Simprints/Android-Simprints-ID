package com.simprints.eventsystem.event.local.migrations

import android.content.ContentValues
import android.database.CursorIndexOutOfBoundsException
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
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.eventsystem.event.local.EventRoomDatabase
import com.simprints.testtools.common.syntax.assertThrows
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

        setupV7DbWithFingerprintCaptureEvent(event = createFingerprintCaptureEvent(eventId))

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val fingerprintCaptureEventJson =
            MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
                .getStringWithColumnName("eventJson")!!

        val fingerprintCaptureBiometricsEventJson =
            MigrationTestingTools.retrieveCursorWithEventByType(
                db,
                FINGERPRINT_CAPTURE_BIOMETRICS
            ).getStringWithColumnName("eventJson")!!

        val fingerprintCaptureEvent =
            JsonHelper.fromJson(fingerprintCaptureEventJson, object : TypeReference<Event>() {})
        val fingerprintCaptureBiometricsEvent =
            JsonHelper.fromJson(
                fingerprintCaptureBiometricsEventJson,
                object : TypeReference<Event>() {})

        val fingerprintCapturePayload =
            JSONObject(fingerprintCaptureEventJson).getJSONObject("payload")
        val fingerprintObject = fingerprintCapturePayload.getJSONObject("fingerprint")

        val fingerprintCaptureBiometricsPayload =
            JSONObject(fingerprintCaptureBiometricsEventJson).getJSONObject("payload")
        val fingerprintBiometricsObject =
            fingerprintCaptureBiometricsPayload.getJSONObject("fingerprint")

        assertThat(fingerprintCaptureEvent).isInstanceOf(FingerprintCaptureEvent::class.java)
        assertThat(fingerprintCaptureEvent.payload.eventVersion).isEqualTo(3)
        assertThat(fingerprintObject.has("template")).isFalse()

        assertThat(fingerprintCaptureBiometricsEvent).isInstanceOf(FingerprintCaptureBiometricsEvent::class.java)
        assertThat(fingerprintCaptureBiometricsEvent.payload.eventVersion).isEqualTo(0)
        assertThat(fingerprintBiometricsObject.has("template")).isTrue()
        assertThat((fingerprintCaptureBiometricsEvent as FingerprintCaptureBiometricsEvent).payload.fingerprint.template).isEqualTo(
            "some_fingerprint_template"
        )
        assertThat(fingerprintCaptureBiometricsEvent.payload.id).isEqualTo(
            (fingerprintCaptureEvent as FingerprintCaptureEvent).payload.id
        )
        assertThat(fingerprintCaptureEvent.labels.projectId).isEqualTo(
            fingerprintCaptureBiometricsEvent.labels.projectId
        )
    }

    @Test
    @Throws(IOException::class)
    fun validateMigrationForFaceCaptureIsSuccessful() {
        val eventId = randomUUID()

        setupV7DbWithFaceCaptureEvent(event = createFaceCaptureEvent(eventId))

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val faceCaptureEventJson = MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        val faceCaptureBiometricsEventJson =
            MigrationTestingTools.retrieveCursorWithEventByType(db, FACE_CAPTURE_BIOMETRICS)
                .getStringWithColumnName("eventJson")!!

        val faceCaptureEvent =
            JsonHelper.fromJson(faceCaptureEventJson, object : TypeReference<Event>() {})
        val faceCaptureBiometricsEvent =
            JsonHelper.fromJson(faceCaptureBiometricsEventJson, object : TypeReference<Event>() {})

        val faceCapturePayload = JSONObject(faceCaptureEventJson).getJSONObject("payload")
        val captureFace = faceCapturePayload.getJSONObject("face")
        val faceCaptureBiometricsPayload =
            JSONObject(faceCaptureBiometricsEventJson).getJSONObject("payload")
        val biometricsFace =
            faceCaptureBiometricsPayload.getJSONObject("face")


        assertThat(faceCaptureEvent).isInstanceOf(FaceCaptureEvent::class.java)
        assertThat(faceCaptureEvent.payload.eventVersion).isEqualTo(3)
        assertThat(captureFace.has("template")).isFalse()

        assertThat(faceCaptureBiometricsEvent).isInstanceOf(FaceCaptureBiometricsEvent::class.java)
        assertThat(faceCaptureBiometricsEvent.payload.eventVersion).isEqualTo(0)
        assertThat(biometricsFace.has("template")).isTrue()
        assertThat((faceCaptureBiometricsEvent as FaceCaptureBiometricsEvent).payload.face.template).isEqualTo(
            "some_face_template"
        )
        assertThat(faceCaptureBiometricsEvent.payload.id).isEqualTo(
            (faceCaptureEvent as FaceCaptureEvent).payload.id
        )
        assertThat(faceCaptureEvent.labels.projectId).isEqualTo(faceCaptureBiometricsEvent.labels.projectId)
    }

    @Test
    @Throws(IOException::class)
    fun validateMigrationDoesNotSaveFaceBiometricsEventsIfNotValid() {
        val eventId = randomUUID()

        setupV7DbWithFaceCaptureEvent(
            event = createFaceCaptureEvent(
                id = eventId,
                result = "INVALID"
            )
        )

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val faceCaptureEventJson = MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        val faceCaptureEvent =
            JsonHelper.fromJson(faceCaptureEventJson, object : TypeReference<Event>() {})

        assertThat(faceCaptureEvent).isInstanceOf(FaceCaptureEvent::class.java)
        assertThat(faceCaptureEvent.payload.eventVersion).isEqualTo(3)
        assertThrows<CursorIndexOutOfBoundsException> {
            MigrationTestingTools.retrieveCursorWithEventByType(db, FACE_CAPTURE_BIOMETRICS)
                .getStringWithColumnName("eventJson")!!
        }
    }

    @Test
    @Throws(IOException::class)
    fun validateMigrationDoesNotSaveFingerprintBiometricsEventsIfNotGoodScan() {
        val eventId = randomUUID()

        setupV7DbWithFingerprintCaptureEvent(
            event = createFingerprintCaptureEvent(
                id = eventId,
                result = "BAD_QUALITY"
            )
        )

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val fingerprintCaptureEventJson = MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        val fingerprintCaptureEvent =
            JsonHelper.fromJson(fingerprintCaptureEventJson, object : TypeReference<Event>() {})

        assertThat(fingerprintCaptureEvent).isInstanceOf(FingerprintCaptureEvent::class.java)
        assertThat(fingerprintCaptureEvent.payload.eventVersion).isEqualTo(3)
        assertThrows<CursorIndexOutOfBoundsException> {
            MigrationTestingTools.retrieveCursorWithEventByType(db, FINGERPRINT_CAPTURE_BIOMETRICS)
                .getStringWithColumnName("eventJson")!!
        }
    }

    private fun setupV7DbWithFaceCaptureEvent(
        close: Boolean = true, event: ContentValues
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 7).apply {
        this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)

        if (close) close()
    }

    private fun setupV7DbWithFingerprintCaptureEvent(
        close: Boolean = true, event: ContentValues
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 7).apply {
        this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)

        if (close) close()
    }

    private fun createFingerprintCaptureEvent(id: String, result: String = "GOOD_SCAN") =
        ContentValues().apply {
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
                    "result":"$result",
                    "fingerprint":{
                        "finger":"LEFT_3RD_FINGER",
                        "quality":0,
                        "template":"some_fingerprint_template",
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

    private fun createFaceCaptureEvent(id: String, result: String = "VALID") =
        ContentValues().apply {
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
                    "result":"$result",
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
        private const val FINGERPRINT_CAPTURE_BIOMETRICS = "FINGERPRINT_CAPTURE_BIOMETRICS"
        private const val FACE_CAPTURE_BIOMETRICS = "FACE_CAPTURE_BIOMETRICS"
    }
}

