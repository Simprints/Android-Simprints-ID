package com.simprints.eventsystem.event.local.migrations

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.utils.randomUUID
import com.simprints.logging.Simber
import org.json.JSONObject

class EventMigration7to8 : Migration(7, 8) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Simber.d("Migrating room db from schema 7 to schema 8.")
            removeTemplateDataFromOldFaceCaptureAndSaveFaceBiometricsEvent(database)
            removeTemplateDataFromOldFingerprintCaptureAndSaveFingerBiometricsEvent(database)
            Simber.d("Migration from schema 7 to schema 8 done.")
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    private fun removeTemplateDataFromOldFingerprintCaptureAndSaveFingerBiometricsEvent(database: SupportSQLiteDatabase) {
        val fingerPrintCaptureQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ?", arrayOf(FINGERPRINT_CAPTURE_EVENT)
        )

        fingerPrintCaptureQuery.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName("id")
                migrateFingerprintCaptureEventPayloadType(it, database, id)
                createAndInsertFingerprintCaptureBiometricsEventValues(database, it)
            }
        }
    }

    /**
     * Remove the template field and update the event version
     */
    private fun migrateFingerprintCaptureEventPayloadType(
        it: Cursor?,
        database: SupportSQLiteDatabase,
        id: String?
    ) {
        val jsonData = it?.getStringWithColumnName(DB_EVENT_JSON_FIELD)

        jsonData?.let {
            val originalJson = JSONObject(it)
            // In the previous system, the event and the payload have matching ids, which is not what we want here
            originalJson.put(DB_ID_FIELD, randomUUID())

            val newPayload = originalJson.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)

            val fingerprint = newPayload.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD_FINGERPRINT)
            fingerprint.remove(TEMPLATE_FIELD)
            newPayload.put(VERSION_PAYLOAD_NAME, NEW_EVENT_VERSION_VALUE)

            val newJson = originalJson.put(DB_EVENT_JSON_EVENT_PAYLOAD, newPayload)
            database.execSQL("UPDATE DbEvent SET eventJson = ? WHERE id = ?", arrayOf(newJson, id))
        }
    }

    /**
     * Remove the template field and update the event version
     */
    private fun removeTemplateDataFromOldFaceCaptureAndSaveFaceBiometricsEvent(database: SupportSQLiteDatabase) {
        val faceCaptureQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ?", arrayOf(FACE_CAPTURE_EVENT)
        )

        faceCaptureQuery.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName("id")
                migrateFaceCaptureEventPayloadType(it, database, id)
                createAndInsertFaceCaptureBiometricsEventValues(database, it)
            }
        }
    }

    /**
     * Remove the template field and update the event version
     */
    private fun migrateFaceCaptureEventPayloadType(
        cursor: Cursor?,
        database: SupportSQLiteDatabase,
        id: String?
    ) {

        val jsonData = cursor?.getStringWithColumnName(DB_EVENT_JSON_FIELD)

        jsonData?.let {
            val originalJson = JSONObject(jsonData)
            // In the previous system, the event and the payload have matching ids, which is not what we want here
            originalJson.put(DB_ID_FIELD, randomUUID())

            val newPayload = originalJson.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)

            val face = newPayload.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD_FACE)
            face.remove(TEMPLATE_FIELD)
            newPayload.put(VERSION_PAYLOAD_NAME, NEW_EVENT_VERSION_VALUE)

            val newJson = originalJson.put(DB_EVENT_JSON_EVENT_PAYLOAD, newPayload)
            database.execSQL("UPDATE DbEvent SET eventJson = ? WHERE id = ?", arrayOf(newJson, id))
        }
    }

    private fun createAndInsertFaceCaptureBiometricsEventValues(
        database: SupportSQLiteDatabase,
        cursor: Cursor?
    ) {
        val originalObject = JSONObject(cursor?.getStringWithColumnName(DB_EVENT_JSON_FIELD)!!)
        val payload = originalObject.getJSONObject("payload")
        val payloadId = payload.getString("id")

        val isFaceEventValid = payload.getString("result") == "VALID"

        if (isFaceEventValid) {
            val faceObject = payload.getJSONObject("face")
            val labels = originalObject.getJSONObject("labels")
            val createdAt = payload.getLong("createdAt")

            val event = "{\"id\":\"${randomUUID()}\",\"labels\":{\"projectId\":\"${
                labels.getString("projectId")
            }\",\"attendantId\":\"${
                labels.getString("attendantId")
            }\",\"moduleIds\":${
                labels.getJSONArray("moduleIds")
            },\"mode\":${
                labels.getJSONArray("mode")
            },\"sessionId\":\"${
                labels.getString("sessionId")
            }\",\"deviceId\":\"${
                labels.getString("deviceId")
            }\"},\"payload\":{\"id\":\"$payloadId\",\"createdAt\":$createdAt,\"eventVersion\":0,\"face\":{\"yaw\":${
                faceObject.getDouble("yaw")
            },\"roll\":${
                faceObject.getDouble("roll")
            },\"template\":\"${
                faceObject.getString("template")
            }\",\"quality\":${
                faceObject.getDouble("quality")
            },\"format\":\"${
                faceObject.getString("format")
            }\"},\"endedAt\":0,\"type\":\"FACE_CAPTURE_BIOMETRICS\"},\"type\":\"FACE_CAPTURE_BIOMETRICS\"}"

            val faceCaptureBiometricsEvent = ContentValues().apply {
                this.put("id", randomUUID())
                this.put("type", FACE_CAPTURE_BIOMETRICS)
                this.put("eventJson", event)
                this.put("createdAt", createdAt)
                this.put("endedAt", 0)
                this.put("sessionIsClosed", 0)
            }

            database.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, faceCaptureBiometricsEvent)
        }
    }

    private fun createAndInsertFingerprintCaptureBiometricsEventValues(
        database: SupportSQLiteDatabase,
        cursor: Cursor?
    ) {
        val originalObject = JSONObject(cursor?.getStringWithColumnName(DB_EVENT_JSON_FIELD)!!)
        val payload = originalObject.getJSONObject("payload")
        val payloadId = payload.getString("id")
        val isFingerprintEventGoodScan = payload.getString("result") == "GOOD_SCAN"

        if (isFingerprintEventGoodScan) {
            val fingerprintObject = payload.getJSONObject("fingerprint")
            val createdAt = payload.getLong("createdAt")
            val labels = originalObject.getJSONObject("labels")

            val event = "{\"id\":\"${randomUUID()}\",\"labels\":{\"projectId\":\"${
                labels.getString("projectId")
            }\",\"attendantId\":\"${
                labels.getString("attendantId")
            }\",\"moduleIds\":${
                labels.getJSONArray("moduleIds")
            },\"mode\":${
                labels.getJSONArray("mode")
            },\"sessionId\":\"${
                labels.getString("sessionId")
            }\",\"deviceId\":\"${
                labels.getString("deviceId")
            }\"},\"payload\":{\"createdAt\":$createdAt,\"eventVersion\":0,\"fingerprint\":{\"finger\":\"${
                fingerprintObject.getString("finger")
            }\",\"template\":\"${
                fingerprintObject.getString("template")
            }\",\"quality\":${
                fingerprintObject.getInt("quality")
            },\"format\":\"${
                fingerprintObject.getString("format")
            }\"},\"id\":\"$payloadId\",\"type\":\"FINGERPRINT_CAPTURE_BIOMETRICS\",\"endedAt\":0},\"type\":\"FINGERPRINT_CAPTURE_BIOMETRICS\"}"

            val fingerprintCaptureBiometricsEvent = ContentValues().apply {
                this.put("id", randomUUID())
                this.put("type", FINGERPRINT_CAPTURE_BIOMETRICS)
                this.put("eventJson", event)
                this.put("createdAt", createdAt)
                this.put("endedAt", 0)
                this.put("sessionIsClosed", 0)
            }

            database.insert(
                "DbEvent",
                SQLiteDatabase.CONFLICT_NONE,
                fingerprintCaptureBiometricsEvent
            )
        }
    }

    companion object {
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
