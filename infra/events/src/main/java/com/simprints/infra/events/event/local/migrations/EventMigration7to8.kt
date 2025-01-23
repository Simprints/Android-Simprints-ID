package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.utils.randomUUID
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import org.json.JSONObject

internal class EventMigration7to8 : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Simber.i("Migrating room db from schema 7 to schema 8.", tag = MIGRATION)
        removeTemplateDataFromOldFingerprintCaptureAndSaveFingerBiometricsEvent(database)
        removeTemplateDataFromOldFaceCaptureAndSaveFaceBiometricsEvent(database)
        Simber.i("Migration from schema 7 to schema 8 done.", tag = MIGRATION)
    }

    private fun removeTemplateDataFromOldFingerprintCaptureAndSaveFingerBiometricsEvent(database: SupportSQLiteDatabase) {
        val fingerprintCaptureQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ?",
            arrayOf(FINGERPRINT_CAPTURE_EVENT),
        )

        fingerprintCaptureQuery.use {
            while (it.moveToNext()) {
                try {
                    val id = it.getStringWithColumnName(DB_ID_FIELD)
                    createAndInsertFingerprintCaptureBiometricsEventValues(database, it, id)
                    migrateFingerprintCaptureEventPayloadType(it, database, id)
                } catch (t: Throwable) {
                    Simber.e(
                        "Fail to migrate fingerprint capture ${
                            it.getStringWithColumnName(DB_ID_FIELD)
                        } in session ${it.getStringWithColumnName("sessionId")}",
                        t,
                        tag = MIGRATION,
                    )
                }
            }
        }
    }

    private fun createAndInsertFingerprintCaptureBiometricsEventValues(
        database: SupportSQLiteDatabase,
        cursor: Cursor?,
        payloadId: String?,
    ) {
        val originalObject = JSONObject(cursor?.getStringWithColumnName(DB_EVENT_JSON_FIELD)!!)
        val payload = originalObject.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)
        val labelsObject = originalObject.getJSONObject("labels")
        val sessionId = labelsObject.getString("sessionId")

        if (!payload.has(DB_EVENT_JSON_EVENT_PAYLOAD_FINGERPRINT)) { // No fingerprint object we can skip
            return
        }

        val isPayloadIdReferencedInPersonCreation =
            isPayloadIdReferencedInPersonCreation(payload, sessionId, database)

        val isFingerprintEventGoodScan = payload.getString("result") == "GOOD_SCAN"

        if (isPayloadIdReferencedInPersonCreation || isFingerprintEventGoodScan) {
            val fingerprintObject = payload.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD_FINGERPRINT)
            val createdAt = payload.getLong("createdAt")
            val eventId = randomUUID()

            val fingerprintCaptureBiometricsEvent = createFingerprintCaptureBiometricsEvent(
                eventId = eventId,
                labelsObject = labelsObject,
                createdAt = createdAt,
                fingerprintObject = fingerprintObject,
                payloadId = payloadId,
            )

            database.insert(
                "DbEvent",
                SQLiteDatabase.CONFLICT_NONE,
                fingerprintCaptureBiometricsEvent,
            )
        }
    }

    private fun isPayloadIdReferencedInPersonCreation(
        payload: JSONObject,
        sessionId: String,
        database: SupportSQLiteDatabase,
    ): Boolean {
        val isFingerprintEventBadQuality = payload.getString("result") == "BAD_QUALITY"
        val payloadId = payload.getString("id")

        if (isFingerprintEventBadQuality) {
            val personCreationQuery = database.query(
                "SELECT * FROM DbEvent WHERE type = ? AND sessionId = ?",
                arrayOf(PERSON_CREATION_EVENT, sessionId),
            )

            personCreationQuery.use {
                while (it.moveToNext()) {
                    val personCreationEvent = JSONObject(
                        it?.getStringWithColumnName(
                            DB_EVENT_JSON_FIELD,
                        )!!,
                    )

                    val personCreationPayload =
                        personCreationEvent.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)
                    val fingerprintCaptureIds =
                        personCreationPayload.getJSONArray("fingerprintCaptureIds").toString()

                    return fingerprintCaptureIds.contains(payloadId)
                }
            }
        }

        return false
    }

    private fun createFingerprintCaptureBiometricsEvent(
        eventId: String,
        labelsObject: JSONObject,
        createdAt: Long,
        fingerprintObject: JSONObject,
        payloadId: String?,
    ): ContentValues {
        val event =
            "{\"id\":\"${eventId}\",\"labels\":$labelsObject,\"payload\":{\"createdAt\":$createdAt,\"eventVersion\":0,\"fingerprint\":{\"finger\":\"${
                fingerprintObject.getString("finger")
            }\",\"template\":\"${
                fingerprintObject.getString("template").replace("\\s".toRegex(), "")
            }\",\"quality\":${
                fingerprintObject.getInt("quality")
            },\"format\":\"${
                fingerprintObject.getString("format")
            }\"},\"id\":\"$payloadId\",\"type\":\"FINGERPRINT_CAPTURE_BIOMETRICS\",\"endedAt\":0},\"type\":\"FINGERPRINT_CAPTURE_BIOMETRICS\"}"

        val fingerprintCaptureBiometricsEvent = ContentValues().apply {
            this.put("id", eventId)
            this.put("projectId", labelsObject.optString("projectId"))
            this.put("sessionId", labelsObject.optString("sessionId"))
            this.put("deviceId", labelsObject.optString("deviceId"))
            this.put("type", FINGERPRINT_CAPTURE_BIOMETRICS)
            this.put("eventJson", event)
            this.put("createdAt", createdAt)
            this.put("endedAt", 0)
            this.put("sessionIsClosed", 0)
        }
        return fingerprintCaptureBiometricsEvent
    }

    /**
     * Remove the template field and update the event version
     */
    private fun migrateFingerprintCaptureEventPayloadType(
        it: Cursor?,
        database: SupportSQLiteDatabase,
        id: String?,
    ) {
        val json = JSONObject(it?.getStringWithColumnName(DB_EVENT_JSON_FIELD)!!)

        val payload = json.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)
        payload.put(VERSION_PAYLOAD_NAME, NEW_EVENT_VERSION_VALUE)

        if (payload.has(DB_EVENT_JSON_EVENT_PAYLOAD_FINGERPRINT)) {
            payload.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD_FINGERPRINT).remove(TEMPLATE_FIELD)
        }

        json.put(DB_EVENT_JSON_EVENT_PAYLOAD, payload)

        database.execSQL("UPDATE DbEvent SET eventJson = ? WHERE id = ?", arrayOf(json, id))
    }

    /**
     * Remove the template field and update the event version
     */
    private fun removeTemplateDataFromOldFaceCaptureAndSaveFaceBiometricsEvent(database: SupportSQLiteDatabase) {
        val faceCaptureQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ?",
            arrayOf(FACE_CAPTURE_EVENT),
        )

        faceCaptureQuery.use {
            while (it.moveToNext()) {
                try {
                    val id = it.getStringWithColumnName(DB_ID_FIELD)
                    createAndInsertFaceCaptureBiometricsEventValues(database, it, id)
                    migrateFaceCaptureEventPayloadType(it, database, id)
                } catch (t: Throwable) {
                    Simber.e(
                        "Fail to migrate face capture ${
                            it.getStringWithColumnName(
                                DB_ID_FIELD,
                            )
                        } in session ${it.getStringWithColumnName("sessionId")}",
                        t,
                    )
                }
            }
        }
    }

    private fun createAndInsertFaceCaptureBiometricsEventValues(
        database: SupportSQLiteDatabase,
        cursor: Cursor?,
        payloadId: String?,
    ) {
        val originalObject = JSONObject(cursor?.getStringWithColumnName(DB_EVENT_JSON_FIELD)!!)
        val payload = originalObject.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)

        if (!payload.has(DB_EVENT_JSON_EVENT_PAYLOAD_FACE)) { // No face
            return
        }

        val isFaceEventValid = payload.getString("result") == "VALID"

        if (!isFaceEventValid) return

        val faceObject = payload.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD_FACE)
        val labelsObject = originalObject.getJSONObject("labels")
        val createdAt = payload.getLong("createdAt")
        val eventId = randomUUID()

        val faceCaptureBiometricsEvent = createFaceCaptureBiometricsEvent(
            eventId = eventId,
            labelsObject = labelsObject,
            createdAt = createdAt,
            faceObject = faceObject,
            payloadId = payloadId,
        )

        database.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, faceCaptureBiometricsEvent)
    }

    private fun createFaceCaptureBiometricsEvent(
        eventId: String,
        labelsObject: JSONObject,
        createdAt: Long,
        faceObject: JSONObject,
        payloadId: String?,
    ): ContentValues {
        val event =
            "{\"id\":\"${eventId}\",\"labels\":$labelsObject,\"payload\":{\"id\":\"$payloadId\",\"createdAt\":$createdAt,\"eventVersion\":0,\"face\":{\"yaw\":${
                faceObject.getDouble("yaw")
            },\"roll\":${
                faceObject.getDouble("roll")
            },\"template\":\"${
                faceObject.getString("template").replace("\\s".toRegex(), "")
            }\",\"quality\":${
                faceObject.getDouble("quality")
            },\"format\":\"${
                faceObject.getString("format")
            }\"},\"endedAt\":0,\"type\":\"FACE_CAPTURE_BIOMETRICS\"},\"type\":\"FACE_CAPTURE_BIOMETRICS\"}"
                .trimIndent()

        return ContentValues().apply {
            this.put("id", eventId)
            this.put("projectId", labelsObject.optString("projectId"))
            this.put("sessionId", labelsObject.optString("sessionId"))
            this.put("deviceId", labelsObject.optString("deviceId"))
            this.put("type", FACE_CAPTURE_BIOMETRICS)
            this.put("eventJson", event)
            this.put("createdAt", createdAt)
            this.put("endedAt", 0)
            this.put("sessionIsClosed", 0)
        }
    }

    /**
     * Remove the template field and update the event version
     */
    private fun migrateFaceCaptureEventPayloadType(
        cursor: Cursor?,
        database: SupportSQLiteDatabase,
        id: String?,
    ) {
        val json = JSONObject(cursor?.getStringWithColumnName(DB_EVENT_JSON_FIELD)!!)

        val payload = json.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)
        payload.put(VERSION_PAYLOAD_NAME, NEW_EVENT_VERSION_VALUE)

        if (payload.has(DB_EVENT_JSON_EVENT_PAYLOAD_FACE)) {
            payload.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD_FACE).remove(TEMPLATE_FIELD)
        }

        json.put(DB_EVENT_JSON_EVENT_PAYLOAD, payload)

        database.execSQL("UPDATE DbEvent SET eventJson = ? WHERE id = ?", arrayOf(json, id))
    }

    companion object {
        private const val PERSON_CREATION_EVENT = "PERSON_CREATION"
        private const val FACE_CAPTURE_EVENT = "FACE_CAPTURE"
        private const val FINGERPRINT_CAPTURE_EVENT = "FINGERPRINT_CAPTURE"
        private const val FINGERPRINT_CAPTURE_BIOMETRICS = "FINGERPRINT_CAPTURE_BIOMETRICS"
        private const val FACE_CAPTURE_BIOMETRICS = "FACE_CAPTURE_BIOMETRICS"
        private const val VERSION_PAYLOAD_NAME = "eventVersion"
        private const val DB_EVENT_JSON_FIELD = "eventJson"
        private const val DB_ID_FIELD = "id"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD = "payload"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD_FACE = "face"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD_FINGERPRINT = "fingerprint"
        private const val TEMPLATE_FIELD = "template"
        private const val NEW_EVENT_VERSION_VALUE = 3
    }
}
