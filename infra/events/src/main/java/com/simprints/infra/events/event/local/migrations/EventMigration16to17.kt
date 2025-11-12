package com.simprints.infra.events.event.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import org.json.JSONArray
import org.json.JSONObject

/**
 * Starting from 2025.4.0, EnrolmentEventV4 requires the `externalCredentialIds` field.
 * This migration adds an empty `externalCredentialIds` array field to the EnrolmentEventV4 event
 *
 * This migration adds:
 *   "externalCredentialIds": [],
 * before the "type" field within the payload
 */
internal class EventMigration16to17 : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Simber.i("Migrating room db from schema 16 to schema 17.", tag = MIGRATION)
        migrateEnrolmentEventJson(db)
        Simber.i("Migration from schema 16 to schema 17 done.", tag = MIGRATION)
    }

    private fun migrateEnrolmentEventJson(database: SupportSQLiteDatabase) {
        val eventsQuery = database.query(
            "SELECT * FROM $DB_EVENT_ENTITY WHERE type = ?",
            arrayOf(EVENT_TYPE_ENROLMENT_V4),
        )
        eventsQuery.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getStringWithColumnName("id") ?: continue
                val jsonData = cursor.getStringWithColumnName(DB_EVENT_JSON_FIELD) ?: continue

                try {
                    val jsonObject = JSONObject(jsonData)
                    val payload = jsonObject.optJSONObject(PAYLOAD_JSON_FIELD) ?: continue

                    // Only adding if 'externalCredentialIds' field doesn't exist
                    if (!payload.has(EXTERNAL_CREDENTIAL_IDS_JSON_FIELD)) {
                        payload.put(EXTERNAL_CREDENTIAL_IDS_JSON_FIELD, JSONArray())
                        val migratedJson = jsonObject.toString()
                        database.execSQL(
                            "UPDATE $DB_EVENT_ENTITY SET $DB_EVENT_JSON_FIELD = ? WHERE id = ?",
                            arrayOf(migratedJson, id),
                        )
                    }
                } catch (e: Exception) {
                    Simber.e(
                        "Failed to migrate room db from schema 16 to schema 17.",
                        e,
                        tag = MIGRATION,
                    )
                }
            }
        }
    }

    companion object {
        private const val DB_EVENT_ENTITY = "DbEvent"
        private const val DB_EVENT_JSON_FIELD = "eventJson"
        private const val EVENT_TYPE_ENROLMENT_V4 = "ENROLMENT_V4"
        private const val EXTERNAL_CREDENTIAL_IDS_JSON_FIELD = "externalCredentialIds"
        private const val PAYLOAD_JSON_FIELD = "payload"
    }
}
