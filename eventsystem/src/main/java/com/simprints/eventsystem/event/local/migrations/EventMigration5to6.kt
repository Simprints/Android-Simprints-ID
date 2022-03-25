package com.simprints.eventsystem.event.local.migrations

import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.logging.Simber
import org.json.JSONObject

class EventMigration5to6 : Migration(5, 6) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Simber.d("Migrating room db from schema 5 to schema 6.")
            removeTemplateDataFromOldFaceCapture(database)
            removeTemplateDataFromOldFingerprintCapture(database)
            Simber.d("Migration from schema 5 to schema 6 done.")
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
                migrateFingerprintCaptureEventPayloadType(it, database, id)
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
            originalJson.remove(OLD_FINGERPRINT_CAPTURE_EVENT)
            originalJson.put(DB_EVENT_TYPE_FIELD, NEW_FINGERPRINT_CAPTURE_EVENT)

            val newPayload = originalJson.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)
            newPayload.remove(OLD_FINGERPRINT_CAPTURE_EVENT)
            newPayload.put(DB_EVENT_TYPE_FIELD, NEW_FINGERPRINT_CAPTURE_EVENT)

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
                val id = it.getStringWithColumnName("id")
                migrateFaceCaptureEventPayloadType(it, database, id)
            }
        }
    }

    /**
     * Remove the template field and update the event version
     */
    private fun migrateFaceCaptureEventPayloadType(
        it: Cursor?,
        database: SupportSQLiteDatabase,
        id: String?
    ) {
        val jsonData = it?.getStringWithColumnName(DB_EVENT_JSON_FIELD)

        jsonData?.let {
            val originalJson = JSONObject(jsonData)
            originalJson.remove(OLD_FACE_CAPTURE_EVENT)
            originalJson.put(DB_EVENT_TYPE_FIELD, NEW_FACE_CAPTURE_EVENT)

            val newPayload = originalJson.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)
            newPayload.remove(OLD_FACE_CAPTURE_EVENT)
            newPayload.put(DB_EVENT_TYPE_FIELD, NEW_FACE_CAPTURE_EVENT)

            val fingerprint = newPayload.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD_FACE)
            fingerprint.remove(PAYLOAD_TYPE_NAME)
            newPayload.put(VERSION_PAYLOAD_NAME, NEW_EVENT_VERSION_VALUE)

            val newJson = originalJson.put(DB_EVENT_JSON_EVENT_PAYLOAD, newPayload)
            database.execSQL("UPDATE DbEvent SET eventJson = ? WHERE id = ?", arrayOf(newJson, id))
        }
    }

    companion object {
        private const val OLD_FACE_CAPTURE_EVENT = "FACE_CAPTURE"
        private const val NEW_FACE_CAPTURE_EVENT = "FACE_CAPTURE_V3"
        private const val OLD_FINGERPRINT_CAPTURE_EVENT = "FINGERPRINT_CAPTURE"
        private const val NEW_FINGERPRINT_CAPTURE_EVENT = "FINGERPRINT_CAPTURE_V3"
        private const val VERSION_PAYLOAD_NAME = "eventVersion"
        private const val DB_EVENT_JSON_FIELD = "eventJson"
        private const val DB_EVENT_TYPE_FIELD = "type"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD = "payload"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD_FACE = "face"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD_FINGERPRINT = "fingerprint"
        private const val PAYLOAD_TYPE_NAME = "template"
        private const val NEW_EVENT_VERSION_VALUE = 3
    }
}
