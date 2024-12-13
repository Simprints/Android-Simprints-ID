package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.events.event.local.EventRoomDatabase
import com.simprints.testtools.common.syntax.assertThrows
import org.json.JSONObject
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

        MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .use { validateFingerprintCapture(it, eventId, "GOOD_SCAN", true) }

        MigrationTestingTools
            .retrieveCursorWithEventByType(db, FINGERPRINT_CAPTURE_BIOMETRICS)
            .use { validateFingerprintBiometricCapture(it, eventId) }

        helper.closeWhenFinished(db)
    }

    @Test
    fun `should migrate fingerprint capture with a BAD_QUALITY mentioned in the PersonCreation and create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(
            events = listOf(
                createFingerprintCaptureEvent(eventId, "BAD_QUALITY"),
                createPersonCreationEvent(eventId),
            ),
        )
        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .use { validateFingerprintCapture(it, eventId, "BAD_QUALITY", true) }

        MigrationTestingTools
            .retrieveCursorWithEventByType(db, FINGERPRINT_CAPTURE_BIOMETRICS)
            .use { validateFingerprintBiometricCapture(it, eventId) }

        helper.closeWhenFinished(db)
    }

    @Test
    fun `should only migrate fingerprint capture with a BAD_QUALITY not mentioned in the PersonCreation and not create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(
            events = listOf(
                createFingerprintCaptureEvent(eventId, "BAD_QUALITY"),
                createPersonCreationEvent("someId"),
            ),
        )
        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .use { validateFingerprintCapture(it, eventId, "BAD_QUALITY", true) }

        assertThrows<CursorIndexOutOfBoundsException> {
            MigrationTestingTools
                .retrieveCursorWithEventByType(db, FINGERPRINT_CAPTURE_BIOMETRICS)
                .getStringWithColumnName("eventJson")!!
        }
        helper.closeWhenFinished(db)
    }

    @Test
    fun `should migrate fingerprint capture with a missing fingerprint and not create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(
            events = listOf(
                createFingerprintCaptureEvent(
                    eventId,
                    "SKIPPED",
                    false,
                ),
            ),
        )
        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .use { validateFingerprintCapture(it, eventId, "SKIPPED", false) }

        assertThrows<CursorIndexOutOfBoundsException> {
            MigrationTestingTools
                .retrieveCursorWithEventByType(db, FINGERPRINT_CAPTURE_BIOMETRICS)
                .getStringWithColumnName("eventJson")!!
        }
        helper.closeWhenFinished(db)
    }

    @Test
    fun `should migrate face capture with a VALID result and create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(events = listOf(createFaceCaptureEvent(eventId)))
        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .use { validateFaceCapture(it, eventId, "VALID", true) }

        MigrationTestingTools
            .retrieveCursorWithEventByType(db, FACE_CAPTURE_BIOMETRICS)
            .use { validateFaceBiometricCapture(it, eventId) }

        helper.closeWhenFinished(db)
    }

    @Test
    fun `should only migrate face capture with a INVALID result and not create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(events = listOf(createFaceCaptureEvent(eventId, "INVALID")))
        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .use { validateFaceCapture(it, eventId, "INVALID", true) }

        assertThrows<CursorIndexOutOfBoundsException> {
            MigrationTestingTools
                .retrieveCursorWithEventByType(db, FACE_CAPTURE_BIOMETRICS)
                .getStringWithColumnName("eventJson")!!
        }

        helper.closeWhenFinished(db)
    }

    @Test
    fun `should migrate face capture with a missing face and not create biometric capture`() {
        val eventId = randomUUID()

        setupV7DbWithEvents(events = listOf(createFaceCaptureEvent(eventId, "INVALID", false)))

        val db = helper.runMigrationsAndValidate(TEST_DB, 8, true, EventMigration7to8())

        MigrationTestingTools
            .retrieveCursorWithEventById(db, eventId)
            .use { validateFaceCapture(it, eventId, "INVALID", false) }

        assertThrows<CursorIndexOutOfBoundsException> {
            MigrationTestingTools
                .retrieveCursorWithEventByType(db, FACE_CAPTURE_BIOMETRICS)
                .getStringWithColumnName("eventJson")!!
        }

        helper.closeWhenFinished(db)
    }

    private fun setupV7DbWithEvents(
        close: Boolean = true,
        events: List<ContentValues>,
    ): SupportSQLiteDatabase = helper.createDatabase(TEST_DB, 7).apply {
        events.forEach { event ->
            this.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, event)
        }

        if (close) close()
    }

    private fun createFingerprintCaptureEvent(
        id: String,
        result: String = "GOOD_SCAN",
        withFingerprint: Boolean = true,
    ) = ContentValues().apply {
        this.put("id", id)
        this.put("type", OLD_FINGERPRINT_CAPTURE_EVENT)

        val fingerprintBlock = if (withFingerprint) {
            """
            ,"fingerprint":{
                "finger":"${FINGER.name}",
                "quality":$QUALITY,
                "template":"$TEMPLATE",
                "format":"ISO_19794_2"
            },
            """.trimIndent()
        } else {
            ","
        }

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
                    "createdAt":$CREATED_AT,
                    "eventVersion":2,
                    "endedAt":$ENDED_AT,
                    "finger":"${FINGER.name}",
                    "qualityThreshold":$QUALITY_THRESHOLD,
                    "result":"$result"
                    $fingerprintBlock
                    "type":"FINGERPRINT_CAPTURE"
                    },
                "type":"FINGERPRINT_CAPTURE"
            }
            """.trimIndent()

        this.put("eventJson", event)
        this.put("createdAt", CREATED_AT)
        this.put("endedAt", ENDED_AT)
        this.put("sessionIsClosed", 0)
    }

    private fun createPersonCreationEvent(fingerprintCaptureId: String) = ContentValues().apply {
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
            }
            """.trimIndent()

        put("eventJson", event)
        put("createdAt", CREATED_AT)
        put("endedAt", 0)
        put("sessionId", SESSION_ID)
        put("sessionIsClosed", 0)
    }

    private fun createFaceCaptureEvent(
        id: String,
        result: String = "VALID",
        withFace: Boolean = true,
    ) = ContentValues().apply {
        put("id", id)
        put("type", OLD_FACE_CAPTURE_EVENT)

        val faceBlock = if (withFace) {
            """
            ,"face":{
                "yaw":$YAW,
                "roll":$ROLL,
                "quality":$QUALITY,
                "template":"$TEMPLATE",
                "format":"$FORMAT"
            },"""
        } else {
            ","
        }

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
                    "createdAt":$CREATED_AT,
                    "endedAt":$ENDED_AT,
                    "eventVersion":2,
                    "attemptNb":0,
                    "qualityThreshold":$QUALITY_THRESHOLD,
                    "result":"$result",
                    "isFallback":false
                    $faceBlock
                    "type":"FACE_CAPTURE"
                },
                "type":"FACE_CAPTURE"
            }
            """.trimIndent()

        put("eventJson", event)
        put("createdAt", 1611584017198)
        put("endedAt", ENDED_AT)
        put("sessionIsClosed", 0)
    }

    private fun createExpectedFingerprintEventJson(
        eventId: String,
        result: String,
        withFingerprint: Boolean,
    ): JSONObject {
        val fingerprintBlock = if (withFingerprint) {
            """
            ,"fingerprint":{
                "finger":"${FINGER.name}",
                "quality":$QUALITY,
                "format":"ISO_19794_2"
            },
            """.trimIndent()
        } else {
            ","
        }

        return JSONObject(
            """
            {
                "id":"$eventId",
                "labels":{
                    "projectId":"$PROJECT_ID",
                    "sessionId": "$SESSION_ID",
                    "deviceId": "$DEVICE_ID"
                },
                "payload":{
                    "id":"$eventId",
                    "createdAt":$CREATED_AT,
                    "eventVersion":3,
                    "endedAt":$ENDED_AT,
                    "finger":"${FINGER.name}",
                    "qualityThreshold":$QUALITY_THRESHOLD,
                    "result":"$result"
                    $fingerprintBlock
                    "type":"FINGERPRINT_CAPTURE"
                },
                "type":"FINGERPRINT_CAPTURE"
            }
            """.trimIndent(),
        )
    }

    private fun validateFingerprintCapture(
        cursor: Cursor,
        eventId: String,
        result: String,
        withFingerprint: Boolean,
    ) {
        val eventJson = JSONObject(cursor.getStringWithColumnName("eventJson")!!)
        val expectedJson = createExpectedFingerprintEventJson(eventId, result, withFingerprint)

        assertThat(eventJson.toString()).isEqualTo(expectedJson.toString())
    }

    private fun validateFingerprintBiometricCapture(
        it: Cursor,
        eventId: String,
    ) {
        val eventJsonPayload = JSONObject(it.getStringWithColumnName("eventJson")!!)
            .getJSONObject("payload")

        assertThat(eventJsonPayload.getString("id")).isEqualTo(eventId)
        assertThat(eventJsonPayload.getString("type")).isEqualTo("FINGERPRINT_CAPTURE_BIOMETRICS")
        assertThat(
            eventJsonPayload.getJSONObject("fingerprint").getString("template"),
        ).isEqualTo(TEMPLATE)
    }

    private fun createExpectedFaceCaptureEventJson(
        eventId: String,
        result: String = "VALID",
        withFace: Boolean,
    ): JSONObject {
        val faceBlock = if (withFace) {
            """
            ,"face":{
                "yaw":$YAW,
                "roll":$ROLL,
                "quality":$QUALITY,
                "format":"$FORMAT"
            },
            """.trimIndent()
        } else {
            ","
        }

        return JSONObject(
            """
            {
                "id":"$eventId",
                "labels":{
                    "projectId":"$PROJECT_ID",
                    "sessionId": "$SESSION_ID",
                    "deviceId": "$DEVICE_ID"
                },
                "payload":{
                    "id":"$eventId",
                    "createdAt":$CREATED_AT,
                    "endedAt":$ENDED_AT,
                    "eventVersion":3,
                    "attemptNb":0,
                    "qualityThreshold":$QUALITY_THRESHOLD,
                    "result":"$result",
                    "isFallback":false
                    $faceBlock
                    "type":"FACE_CAPTURE"
                },
                "type":"FACE_CAPTURE"
            }
            """.trimIndent(),
        )
    }

    private fun validateFaceCapture(
        cursor: Cursor,
        eventId: String,
        result: String,
        withFace: Boolean,
    ) {
        val eventJson = JSONObject(cursor.getStringWithColumnName("eventJson")!!)
        val expectedJson = createExpectedFaceCaptureEventJson(eventId, result, withFace)

        assertThat(eventJson.toString()).isEqualTo(expectedJson.toString())
    }

    private fun validateFaceBiometricCapture(
        cursor: Cursor,
        eventId: String,
    ) {
        val eventJsonPayload = JSONObject(cursor.getStringWithColumnName("eventJson")!!)
            .getJSONObject("payload")

        assertThat(eventJsonPayload.getString("id")).isEqualTo(eventId)
        assertThat(eventJsonPayload.getString("type")).isEqualTo("FACE_CAPTURE_BIOMETRICS")
        assertThat(eventJsonPayload.getJSONObject("face").getString("template"))
            .isEqualTo(TEMPLATE)
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
        private val CREATED_AT = 1611584017198
        private val ENDED_AT = 1621588617198
        private const val QUALITY = 85
        private const val QUALITY_THRESHOLD = 60
        private const val YAW = 1.2f
        private const val ROLL = 2.3f
        private const val FORMAT = "ISO_19794_2"
    }
}
