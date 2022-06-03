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
            removeTemplateDataFromOldFaceCapture(database)
            removeTemplateDataFromOldFingerprintCapture(database)
            Simber.d("Migration from schema 7 to schema 8 done.")
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    private fun removeTemplateDataFromOldFingerprintCapture(database: SupportSQLiteDatabase) {
        val fingerPrintCaptureQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ?", arrayOf(OLD_FINGERPRINT_CAPTURE_EVENT)
        )

        fingerPrintCaptureQuery.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName("id")
                val payloadId = randomUUID()
                migrateFingerprintCaptureEventPayloadType(it, database, id, payloadId)
                createAndInsertFingerprintCaptureBiometricsEventValues(database, it, payloadId)
            }
        }
    }

    /**
     * Remove the template field and update the event version
     */
    private fun migrateFingerprintCaptureEventPayloadType(
        it: Cursor?,
        database: SupportSQLiteDatabase,
        id: String?,
        payloadId: String
    ) {
        val jsonData = it?.getStringWithColumnName(DB_EVENT_JSON_FIELD)

        jsonData?.let {
            val originalJson = JSONObject(it)
            originalJson.remove(OLD_FINGERPRINT_CAPTURE_EVENT)
            originalJson.put(DB_EVENT_TYPE_FIELD, NEW_FINGERPRINT_CAPTURE_EVENT)

            val newPayload = originalJson.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)
            newPayload.remove(OLD_FINGERPRINT_CAPTURE_EVENT)
            newPayload.put(DB_EVENT_TYPE_FIELD, NEW_FINGERPRINT_CAPTURE_EVENT)
            newPayload.put(DB_ID_FIELD, payloadId)

            val fingerprint = newPayload.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD_FINGERPRINT)
            fingerprint.remove(PAYLOAD_TYPE_NAME)
            newPayload.put(VERSION_PAYLOAD_NAME, NEW_EVENT_VERSION_VALUE)

            val newJson = originalJson.put(DB_EVENT_JSON_EVENT_PAYLOAD, newPayload)
            database.execSQL("UPDATE DbEvent SET eventJson = ? WHERE id = ?", arrayOf(newJson, id))
        }
    }

    /**
     * Remove the template field and update the event version
     */
    private fun removeTemplateDataFromOldFaceCapture(database: SupportSQLiteDatabase) {
        val faceCaptureQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ?", arrayOf(OLD_FACE_CAPTURE_EVENT)
        )

        faceCaptureQuery.use {
            while (it.moveToNext()) {
                val payloadId = randomUUID()
                val id = it.getStringWithColumnName("id")
                migrateFaceCaptureEventPayloadType(it, database, id, payloadId)
                createAndInsertFaceCaptureBiometricsEventValues(database, it, payloadId)
            }
        }
    }

    /**
     * Remove the template field and update the event version
     */
    private fun migrateFaceCaptureEventPayloadType(
        cursor: Cursor?,
        database: SupportSQLiteDatabase,
        id: String?,
        payloadId: String
    ) {

        val jsonData = cursor?.getStringWithColumnName(DB_EVENT_JSON_FIELD)

        jsonData?.let {
            val originalJson = JSONObject(jsonData)
            originalJson.remove(OLD_FACE_CAPTURE_EVENT)
            originalJson.put(DB_EVENT_TYPE_FIELD, NEW_FACE_CAPTURE_EVENT)

            val newPayload = originalJson.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)
            newPayload.remove(OLD_FACE_CAPTURE_EVENT)
            newPayload.put(DB_EVENT_TYPE_FIELD, NEW_FACE_CAPTURE_EVENT)
            newPayload.put(DB_ID_FIELD, payloadId)

            val face = newPayload.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD_FACE)
            face.remove(PAYLOAD_TYPE_NAME)
            newPayload.put(VERSION_PAYLOAD_NAME, NEW_EVENT_VERSION_VALUE)

            val newJson = originalJson.put(DB_EVENT_JSON_EVENT_PAYLOAD, newPayload)
            database.execSQL("UPDATE DbEvent SET eventJson = ? WHERE id = ?", arrayOf(newJson, id))
        }
    }

    private fun createAndInsertFaceCaptureBiometricsEventValues(
        database: SupportSQLiteDatabase,
        cursor: Cursor?,
        payloadId: String
    ) {
        val originalObject = JSONObject(cursor?.getStringWithColumnName(DB_EVENT_JSON_FIELD)!!)
        val payload = originalObject.getJSONObject("payload")
        val faceObject = payload.getJSONObject("face")
        val labels = originalObject.getJSONObject("labels")
        val createdAt = payload.getLong("createdAt")
        val eventId = randomUUID()

        val event = "{\"id\":\"${randomUUID()}\",\"labels\":{\"moduleIds\":[],\"projectId\":\"${
            labels.getString("projectId")
        }\",\"mode\":[]},\"payload\":{\"id\":\"$payloadId\",\"createdAt\":$createdAt,\"eventVersion\":0,\"face\":{\"template\":\"${
            faceObject.getString(
                "template"
            )
        }\",\"quality\":${faceObject.getDouble("quality")},\"format\":\"RANK_ONE_1_23\"},\"endedAt\":0,\"type\":\"FACE_CAPTURE_BIOMETRICS\"},\"type\":\"FACE_CAPTURE_BIOMETRICS\"}"

        val faceCaptureBiometricsEvent = ContentValues().apply {
            this.put("id", eventId)
            this.put("type", FACE_CAPTURE_BIOMETRICS)
            this.put("eventJson", event)
            this.put("createdAt", createdAt)
            this.put("endedAt", 0)
            this.put("sessionIsClosed", 0)
        }

        database.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, faceCaptureBiometricsEvent)
    }

    private fun createAndInsertFingerprintCaptureBiometricsEventValues(
        database: SupportSQLiteDatabase,
        cursor: Cursor?,
        payloadId: String
    ) {
        val originalObject = JSONObject(cursor?.getStringWithColumnName(DB_EVENT_JSON_FIELD)!!)
        val payload = originalObject.getJSONObject("payload")
        val fingerprintObject = payload.getJSONObject("fingerprint")
        val createdAt = payload.getLong("createdAt")
        val labels = originalObject.getJSONObject("labels")
        val eventId = randomUUID()

        val event =
            "{\"id\":\"${randomUUID()}\",\"labels\":{\"projectId\":\"${
                labels.getString("projectId")
            }\"},\"payload\":{\"createdAt\":$createdAt,\"eventVersion\":0,\"fingerprint\":{\"finger\":\"${fingerprintObject.getString("finger")}\",\"template\":\"${
                fingerprintObject.getString(
                    "template"
                )
            }\",\"quality\":${fingerprintObject.getInt("quality")},\"format\":\"${fingerprintObject.getString("format")}\"},\"id\":\"$payloadId\",\"type\":\"FINGERPRINT_CAPTURE_BIOMETRICS\",\"endedAt\":0},\"type\":\"FINGERPRINT_CAPTURE_BIOMETRICS\"}"

        val faceCaptureBiometricsEvent = ContentValues().apply {
            this.put("id", eventId)
            this.put("type", FINGERPRINT_CAPTURE_BIOMETRICS)
            this.put("eventJson", event)
            this.put("createdAt", createdAt)
            this.put("endedAt", 0)
            this.put("sessionIsClosed", 0)
        }

        database.insert("DbEvent", SQLiteDatabase.CONFLICT_NONE, faceCaptureBiometricsEvent)
    }

    companion object {
        private const val OLD_FACE_CAPTURE_EVENT = "FACE_CAPTURE"
        private const val NEW_FACE_CAPTURE_EVENT = "FACE_CAPTURE_V3"
        private const val OLD_FINGERPRINT_CAPTURE_EVENT = "FINGERPRINT_CAPTURE"
        private const val NEW_FINGERPRINT_CAPTURE_EVENT = "FINGERPRINT_CAPTURE_V3"
        private const val FINGERPRINT_CAPTURE_BIOMETRICS = "FINGERPRINT_CAPTURE_BIOMETRICS"
        private const val FACE_CAPTURE_BIOMETRICS = "FACE_CAPTURE_BIOMETRICS"
        private const val VERSION_PAYLOAD_NAME = "eventVersion"
        private const val DB_EVENT_JSON_FIELD = "eventJson"
        private const val DB_EVENT_TYPE_FIELD = "type"
        private const val DB_ID_FIELD = "id"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD = "payload"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD_FACE = "face"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD_FINGERPRINT = "fingerprint"
        private const val PAYLOAD_TYPE_NAME = "template"
        private const val NEW_EVENT_VERSION_VALUE = 3
    }
}
