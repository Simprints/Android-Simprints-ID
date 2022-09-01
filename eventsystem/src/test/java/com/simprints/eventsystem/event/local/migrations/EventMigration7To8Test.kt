package com.simprints.eventsystem.event.local.migrations

import android.content.ContentValues
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.eventsystem.event.local.EventRoomDatabase
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventMigration7To8Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EventRoomDatabase::class.java,
    )

    @Test
    fun `should migrate fingerprint capture with a GOOD_SCAN and create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(events = listOf(createFingerprintCaptureEvent(eventId)))

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val fingerprintCaptureEventJson =
            MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
                .getStringWithColumnName("eventJson")!!

        val fingerprintCaptureEvent = JsonHelper.fromJson(
            fingerprintCaptureEventJson,
            object : TypeReference<Event>() {}
        )

        val expectedFingerprintCaptureEvent = FingerprintCaptureEvent(
            id = eventId,
            labels = EventLabels(
                projectId = PROJECT_ID,
                sessionId = SESSION_ID,
                deviceId = DEVICE_ID
            ),
            payload = FingerprintCaptureEvent.FingerprintCapturePayload(
                CREATED_AT,
                3,
                ENDED_AT,
                FINGER,
                QUALITY_THRESHOLD,
                FingerprintCaptureEvent.FingerprintCapturePayload.Result.GOOD_SCAN,
                FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(FINGER, QUALITY),
                eventId
            ),
            type = EventType.FINGERPRINT_CAPTURE,
        )

        assertThat(fingerprintCaptureEvent).isEqualTo(expectedFingerprintCaptureEvent)

        val fingerprintCaptureBiometricsEventJson = MigrationTestingTools
            .retrieveCursorWithEventByType(db, FINGERPRINT_CAPTURE_BIOMETRICS)
            .getStringWithColumnName("eventJson")!!


        val fingerprintCaptureBiometricsEvent = JsonHelper.fromJson(
            fingerprintCaptureBiometricsEventJson,
            object : TypeReference<Event>() {}
        )

        val expectedFingerprintCaptureBiometricsEvent = FingerprintCaptureBiometricsEvent(
            labels = EventLabels(
                projectId = PROJECT_ID,
                sessionId = SESSION_ID,
                deviceId = DEVICE_ID
            ),
            payload = FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload(
                CREATED_AT,
                0,
                FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(
                    FINGER,
                    TEMPLATE,
                    QUALITY,
                ),
                eventId
            ),
            type = EventType.FINGERPRINT_CAPTURE_BIOMETRICS,
        )

        assertThat(fingerprintCaptureBiometricsEvent.labels).isEqualTo(
            expectedFingerprintCaptureBiometricsEvent.labels
        )
        assertThat(fingerprintCaptureBiometricsEvent.payload).isEqualTo(
            expectedFingerprintCaptureBiometricsEvent.payload
        )
        assertThat(fingerprintCaptureBiometricsEvent.type).isEqualTo(
            expectedFingerprintCaptureBiometricsEvent.type
        )
    }

    @Test
    fun `should migrate fingerprint capture with a BAD_QUALITY mentioned in the PersonCreation and create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(
            events = listOf(
                createFingerprintCaptureEvent(eventId, "BAD_QUALITY"),
                createPersonCreationEvent(eventId)
            )
        )

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val fingerprintCaptureEventJson =
            MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
                .getStringWithColumnName("eventJson")!!

        val fingerprintCaptureEvent = JsonHelper.fromJson(
            fingerprintCaptureEventJson,
            object : TypeReference<Event>() {}
        )

        val expectedFingerprintCaptureEvent = FingerprintCaptureEvent(
            id = eventId,
            labels = EventLabels(
                projectId = PROJECT_ID,
                sessionId = SESSION_ID,
                deviceId = DEVICE_ID
            ),
            payload = FingerprintCaptureEvent.FingerprintCapturePayload(
                CREATED_AT,
                3,
                ENDED_AT,
                FINGER,
                QUALITY_THRESHOLD,
                FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY,
                FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(FINGER, QUALITY),
                eventId
            ),
            type = EventType.FINGERPRINT_CAPTURE,
        )

        assertThat(fingerprintCaptureEvent).isEqualTo(expectedFingerprintCaptureEvent)

        val fingerprintCaptureBiometricsEventJson = MigrationTestingTools
            .retrieveCursorWithEventByType(db, FINGERPRINT_CAPTURE_BIOMETRICS)
            .getStringWithColumnName("eventJson")!!


        val fingerprintCaptureBiometricsEvent = JsonHelper.fromJson(
            fingerprintCaptureBiometricsEventJson,
            object : TypeReference<Event>() {}
        )

        val expectedFingerprintCaptureBiometricsEvent = FingerprintCaptureBiometricsEvent(
            labels = EventLabels(
                projectId = PROJECT_ID,
                sessionId = SESSION_ID,
                deviceId = DEVICE_ID
            ),
            payload = FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload(
                CREATED_AT,
                0,
                FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(
                    FINGER,
                    TEMPLATE,
                    QUALITY,
                ),
                eventId
            ),
            type = EventType.FINGERPRINT_CAPTURE_BIOMETRICS,
        )

        assertThat(fingerprintCaptureBiometricsEvent.labels).isEqualTo(
            expectedFingerprintCaptureBiometricsEvent.labels
        )
        assertThat(fingerprintCaptureBiometricsEvent.payload).isEqualTo(
            expectedFingerprintCaptureBiometricsEvent.payload
        )
        assertThat(fingerprintCaptureBiometricsEvent.type).isEqualTo(
            expectedFingerprintCaptureBiometricsEvent.type
        )
    }

    @Test
    fun `should only migrate fingerprint capture with a BAD_QUALITY not mentioned in the PersonCreation and not create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(
            events = listOf(
                createFingerprintCaptureEvent(eventId, "BAD_QUALITY"),
                createPersonCreationEvent("someId")
            )
        )

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val fingerprintCaptureEventJson =
            MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
                .getStringWithColumnName("eventJson")!!

        val fingerprintCaptureEvent = JsonHelper.fromJson(
            fingerprintCaptureEventJson,
            object : TypeReference<Event>() {}
        )

        val expectedFingerprintCaptureEvent = FingerprintCaptureEvent(
            id = eventId,
            labels = EventLabels(
                projectId = PROJECT_ID,
                sessionId = SESSION_ID,
                deviceId = DEVICE_ID
            ),
            payload = FingerprintCaptureEvent.FingerprintCapturePayload(
                CREATED_AT,
                3,
                ENDED_AT,
                FINGER,
                QUALITY_THRESHOLD,
                FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY,
                FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(FINGER, QUALITY),
                eventId
            ),
            type = EventType.FINGERPRINT_CAPTURE,
        )

        assertThat(fingerprintCaptureEvent).isEqualTo(expectedFingerprintCaptureEvent)

        assertThrows<CursorIndexOutOfBoundsException> {
            MigrationTestingTools
                .retrieveCursorWithEventByType(db, FINGERPRINT_CAPTURE_BIOMETRICS)
                .getStringWithColumnName("eventJson")!!
        }
    }

    @Test
    fun `should migrate fingerprint capture with a missing fingerprint and not create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(
            events = listOf(
                createFingerprintCaptureEvent(
                    eventId,
                    "SKIPPED",
                    false
                )
            )
        )

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val fingerprintCaptureEventJson =
            MigrationTestingTools.retrieveCursorWithEventById(db, eventId)
                .getStringWithColumnName("eventJson")!!

        val fingerprintCaptureEvent = JsonHelper.fromJson(
            fingerprintCaptureEventJson,
            object : TypeReference<Event>() {}
        )

        val expectedFingerprintCaptureEvent = FingerprintCaptureEvent(
            id = eventId,
            labels = EventLabels(
                projectId = PROJECT_ID,
                sessionId = SESSION_ID,
                deviceId = DEVICE_ID
            ),
            payload = FingerprintCaptureEvent.FingerprintCapturePayload(
                CREATED_AT,
                3,
                ENDED_AT,
                FINGER,
                QUALITY_THRESHOLD,
                FingerprintCaptureEvent.FingerprintCapturePayload.Result.SKIPPED,
                null,
                eventId
            ),
            type = EventType.FINGERPRINT_CAPTURE,
        )

        assertThat(fingerprintCaptureEvent).isEqualTo(expectedFingerprintCaptureEvent)

        assertThrows<CursorIndexOutOfBoundsException> {
            MigrationTestingTools
                .retrieveCursorWithEventByType(db, FINGERPRINT_CAPTURE_BIOMETRICS)
                .getStringWithColumnName("eventJson")!!
        }
    }

    @Test
    fun `should migrate face capture with a VALID result and create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(events = listOf(createFaceCaptureEvent(eventId)))

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val faceCaptureEventJson = MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        val faceCaptureEvent = JsonHelper.fromJson(
            faceCaptureEventJson,
            object : TypeReference<Event>() {}
        )

        val expectedFaceCaptureEvent = FaceCaptureEvent(
            id = eventId,
            labels = EventLabels(
                projectId = PROJECT_ID,
                sessionId = SESSION_ID,
                deviceId = DEVICE_ID
            ),
            payload = FaceCaptureEvent.FaceCapturePayload(
                eventId,
                CREATED_AT,
                ENDED_AT,
                3,
                0,
                QUALITY_THRESHOLD.toFloat(),
                FaceCaptureEvent.FaceCapturePayload.Result.VALID,
                false,
                FaceCaptureEvent.FaceCapturePayload.Face(YAW, ROLL, QUALITY.toFloat()),
            ),
            type = EventType.FACE_CAPTURE,
        )

        assertThat(faceCaptureEvent).isEqualTo(expectedFaceCaptureEvent)

        val faceCaptureBiometricsEventJson = MigrationTestingTools
            .retrieveCursorWithEventByType(db, FACE_CAPTURE_BIOMETRICS)
            .getStringWithColumnName("eventJson")!!

        val faceCaptureBiometricsEvent = JsonHelper.fromJson(
            faceCaptureBiometricsEventJson,
            object : TypeReference<Event>() {}
        )

        val expectedFaceCaptureBiometricsEvent = FaceCaptureBiometricsEvent(
            labels = EventLabels(
                projectId = PROJECT_ID,
                sessionId = SESSION_ID,
                deviceId = DEVICE_ID
            ),
            payload = FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload(
                eventId,
                CREATED_AT,
                0,
                FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
                    YAW,
                    ROLL,
                    TEMPLATE,
                    QUALITY.toFloat()
                ),
                0,
            ),
            type = EventType.FACE_CAPTURE_BIOMETRICS,
        )

        assertThat(faceCaptureBiometricsEvent.labels).isEqualTo(expectedFaceCaptureBiometricsEvent.labels)
        assertThat(faceCaptureBiometricsEvent.payload).isEqualTo(expectedFaceCaptureBiometricsEvent.payload)
        assertThat(faceCaptureBiometricsEvent.type).isEqualTo(expectedFaceCaptureBiometricsEvent.type)
    }

    @Test
    fun `should only migrate face capture with a INVALID result and not create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(events = listOf(createFaceCaptureEvent(eventId, "INVALID")))

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val faceCaptureEventJson = MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        val faceCaptureEvent = JsonHelper.fromJson(
            faceCaptureEventJson,
            object : TypeReference<Event>() {}
        )

        val expectedFaceCaptureEvent = FaceCaptureEvent(
            id = eventId,
            labels = EventLabels(
                projectId = PROJECT_ID,
                sessionId = SESSION_ID,
                deviceId = DEVICE_ID
            ),
            payload = FaceCaptureEvent.FaceCapturePayload(
                eventId,
                CREATED_AT,
                ENDED_AT,
                3,
                0,
                QUALITY_THRESHOLD.toFloat(),
                FaceCaptureEvent.FaceCapturePayload.Result.INVALID,
                false,
                FaceCaptureEvent.FaceCapturePayload.Face(YAW, ROLL, QUALITY.toFloat()),
            ),
            type = EventType.FACE_CAPTURE,
        )

        assertThat(faceCaptureEvent).isEqualTo(expectedFaceCaptureEvent)

        assertThrows<CursorIndexOutOfBoundsException> {
            MigrationTestingTools
                .retrieveCursorWithEventByType(db, FACE_CAPTURE_BIOMETRICS)
                .getStringWithColumnName("eventJson")!!
        }
    }

    @Test
    fun `should migrate face capture with a missing face and not create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(events = listOf(createFaceCaptureEvent(eventId, "INVALID", false)))

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        val faceCaptureEventJson = MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .getStringWithColumnName("eventJson")!!

        val faceCaptureEvent = JsonHelper.fromJson(
            faceCaptureEventJson,
            object : TypeReference<Event>() {}
        )

        val expectedFaceCaptureEvent = FaceCaptureEvent(
            id = eventId,
            labels = EventLabels(
                projectId = PROJECT_ID,
                sessionId = SESSION_ID,
                deviceId = DEVICE_ID
            ),
            payload = FaceCaptureEvent.FaceCapturePayload(
                eventId,
                CREATED_AT,
                ENDED_AT,
                3,
                0,
                QUALITY_THRESHOLD.toFloat(),
                FaceCaptureEvent.FaceCapturePayload.Result.INVALID,
                false,
                null,
            ),
            type = EventType.FACE_CAPTURE,
        )

        assertThat(faceCaptureEvent).isEqualTo(expectedFaceCaptureEvent)

        assertThrows<CursorIndexOutOfBoundsException> {
            MigrationTestingTools
                .retrieveCursorWithEventByType(db, FACE_CAPTURE_BIOMETRICS)
                .getStringWithColumnName("eventJson")!!
        }
    }

    private fun setupV7DbWithEvents(
        close: Boolean = true, events: List<ContentValues>
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 7).apply {

        events.forEach { event ->
            this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)
        }

        if (close) close()
    }

    private fun createFingerprintCaptureEvent(
        id: String,
        result: String = "GOOD_SCAN",
        withFingerprint: Boolean = true
    ) =
        ContentValues().apply {
            this.put("id", id)
            this.put("type", OLD_FINGERPRINT_CAPTURE_EVENT)

            val event =
                """
                {
                "id":"$id",
                "labels":{
                    "projectId":"$PROJECT_ID",
                    "sessionId": "$SESSION_ID",
                    "deviceId": "$DEVICE_ID"
                },
                "payload":{
                    "createdAt":$CREATED_AT,
                    "eventVersion":2,
                    "endedAt":$ENDED_AT,
                    "finger":"${FINGER.name}",
                    "qualityThreshold":$QUALITY_THRESHOLD,
                    "result":"$result"
                    """ + withFingerprint.let {
                    if (it)
                        """
                            ,"fingerprint":{
                                "finger":"${FINGER.name}",
                                "quality":$QUALITY,
                                "template":"$TEMPLATE",
                                "format":"ISO_19794_2"
                            },"""
                    else ","
                } + """
                "id":"$id",
                "type":"FINGERPRINT_CAPTURE"
                        },
                "type":"FINGERPRINT_CAPTURE"}
                """
                    .trimIndent()

            this.put("eventJson", event)
            this.put("createdAt", CREATED_AT)
            this.put("endedAt", ENDED_AT)
            this.put("sessionIsClosed", 0)
        }

    private fun createPersonCreationEvent(fingerprintCaptureId: String) =
        ContentValues().apply {
            put("id", randomUUID())
            put("type", PERSON_CREATION_EVENT)
            val event =
                """
                {
                "id":"cd088b0e-bd15-4b81-a7eb-161e565e1687",
                "labels":{
                    "projectId":"$PROJECT_ID",
                    "sessionId": "$SESSION_ID",
                    "deviceId": "$DEVICE_ID"
                },
                "payload":{
                    "createdAt":$CREATED_AT,
                    "endedAt":0,
                    "eventVersion":1,
                    "type":"PERSON_CREATION",
                    "fingerprintCaptureIds":[$fingerprintCaptureId,"id2"],
                    "fingerprintReferenceId":"someFingerprintReferenceId",
                    "faceCaptureIds":["id","id2"],
                    "faceReferenceId":"someFaceReferenceId"
                },
                "type":"PERSON_CREATION"
                }"""
                    .trimIndent()

            put("eventJson", event)
            put("createdAt", CREATED_AT)
            put("endedAt", 0)
            put("sessionId", SESSION_ID)
            put("sessionIsClosed", 0)
        }

    private fun createFaceCaptureEvent(
        id: String,
        result: String = "VALID",
        withFace: Boolean = true
    ) =
        ContentValues().apply {
            put("id", id)
            put("type", OLD_FACE_CAPTURE_EVENT)

            val event =
                """
                {
                "id":"$id",
                "labels":{
                    "projectId":"$PROJECT_ID",
                    "sessionId": "$SESSION_ID",
                    "deviceId": "$DEVICE_ID"
                },
                "payload":{
                    "id":"$id",
                    "createdAt":1611584017198,
                    "endedAt":$ENDED_AT,
                    "eventVersion":2,
                    "attemptNb":0,
                    "qualityThreshold":$QUALITY_THRESHOLD,
                    "result":"$result",
                    "isFallback":false
                    """ + withFace.let {
                    if (it)
                        """
                        ,"face":{
                            "yaw":$YAW,
                            "roll":$ROLL,
                            "quality":$QUALITY,
                            "template":"$TEMPLATE",
                            "format":"RANK_ONE_1_23"
                        },"""
                    else ","
                } + """
                    "type":"FACE_CAPTURE"
                },
                "type":"FACE_CAPTURE"
                }"""
                    .trimIndent()

            put("eventJson", event)
            put("createdAt", 1611584017198)
            put("endedAt", ENDED_AT)
            put("sessionIsClosed", 0)
        }

    companion object {
        private const val TEST_DB = "some_db"
        private const val OLD_FACE_CAPTURE_EVENT = "FACE_CAPTURE"
        private const val OLD_FINGERPRINT_CAPTURE_EVENT = "FINGERPRINT_CAPTURE"
        private const val FINGERPRINT_CAPTURE_BIOMETRICS = "FINGERPRINT_CAPTURE_BIOMETRICS"
        private const val FACE_CAPTURE_BIOMETRICS = "FACE_CAPTURE_BIOMETRICS"
        private const val PERSON_CREATION_EVENT = "PERSON_CREATION"
        private const val PROJECT_ID = "aProjectID"
        private const val SESSION_ID = "aSessionID"
        private const val DEVICE_ID = "aDeviceID"
        private const val TEMPLATE = "template"
        private val FINGER = IFingerIdentifier.LEFT_3RD_FINGER
        private const val CREATED_AT = 1611584017198
        private const val ENDED_AT = 1621588617198
        private const val QUALITY = 85
        private const val QUALITY_THRESHOLD = 60
        private const val YAW = 1.2f
        private const val ROLL = 2.3f
    }
}


