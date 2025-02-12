package com.simprints.infra.events.event.local.migrations

import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import org.json.JSONObject

/**
 * The 2021.1.0 (room v2) introduced:
 * 1) Fingenrprint/Face templates format in Fingerprint/FaceApi capture and EnrolmentCreationApi:
 * not a breaking changes for domain/db. Template format is added in the transformations to the API classes.
 * 2) EnrolmentEvent merged with EnrolmentCreationEvent:
 * Added a new class EnrolmentEventV2. The original EnrolmentEvent is now called EnrolmentEventV1.
 * To serialise/deserialize the events stored from previous versions of SID, they need to
 * have ENROLMENT_V1 as type, instead of ENROLMENT. Migration required.
 * 3) Adding a sessionIsClosed field to the SessionCaptureEvent domain class in order to stop using
 * endedAt to mark a sessiona as closed.
 */
internal class EventMigration1to2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Simber.i("Migrating room db from schema 1 to schema 2.", tag = MIGRATION)
            migrateEnrolments(database)
            migrateSessionClosedInformation(database)
            Simber.i("Migration from schema 1 to schema 2 done.", tag = MIGRATION)
        } catch (t: Throwable) {
            Simber.e("Failed to migrate room db from schema 1 to schema 2.", t, tag = MIGRATION)
        }
    }

    private fun migrateEnrolments(database: SupportSQLiteDatabase) {
        val enrolmentEventsQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ?",
            arrayOf(
                OLD_ENROLMENT_EVENT_TYPE,
            ),
        )
        enrolmentEventsQuery.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName("id")
                migrateEnrolmentEventType(id, database)
                migrateEnrolmentEventPayloadType(it, database, id)
            }
        }
    }

    /**
     * Initially an enrolment event wasn't included in a session. This takes the old un-versioned
     * enrolment and re-saves their payloads as ENROLMENT_V1.
     */
    private fun migrateEnrolmentEventPayloadType(
        it: Cursor,
        database: SupportSQLiteDatabase,
        id: String?,
    ) {
        val jsonData = it.getStringWithColumnName(DB_EVENT_JSON_FIELD)
        jsonData?.let {
            val originalJson = JSONObject(jsonData).put(DB_EVENT_JSON_EVENT_TYPE, "ENROLMENT_V1")
            val newPayload = originalJson.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD).put(
                DB_EVENT_JSON_EVENT_TYPE,
                "ENROLMENT_V1",
            )
            val newJson = originalJson.put(DB_EVENT_JSON_EVENT_PAYLOAD, newPayload)
            database.execSQL("UPDATE DbEvent SET eventJson = ? WHERE id = ?", arrayOf(newJson, id))
        }
    }

    private fun migrateEnrolmentEventType(
        id: String?,
        database: SupportSQLiteDatabase,
    ) {
        id?.let {
            database.execSQL("UPDATE DbEvent SET type = ? WHERE id = ?", arrayOf("ENROLMENT_V1", it))
        }
    }

    /**
     * In 2021.1.0 we are moving away from closing sessions by time, and now have an explicit
     * flag that marks them as closed. In order to be able to upload old sessions right after an app
     * update we need to migrate the SessionCaptureEvent to having sessions with an endedAt time > 0
     * being marked as closed.
     */
    private fun migrateSessionClosedInformation(database: SupportSQLiteDatabase) {
        val sessionCaptureEventsQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ? AND endedAt > 0",
            arrayOf("SESSION_CAPTURE"),
        )

        sessionCaptureEventsQuery.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName("id")
                val jsonData = it.getStringWithColumnName(DB_EVENT_JSON_FIELD)
                jsonData?.let {
                    val originalJson = JSONObject(jsonData)
                    val newPayload = originalJson.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD).put(
                        DB_EVENT_PAYLOAD_SESSION_STATUS,
                        true,
                    )
                    val newJson = originalJson.put(DB_EVENT_JSON_EVENT_PAYLOAD, newPayload)
                    database.execSQL(
                        "UPDATE DbEvent SET eventJson = ? WHERE id = ?",
                        arrayOf(newJson, id),
                    )
                }
            }
        }
    }

    companion object {
        private const val OLD_ENROLMENT_EVENT_TYPE = "ENROLMENT"
        private const val DB_EVENT_JSON_FIELD = "eventJson"
        private const val DB_EVENT_JSON_EVENT_TYPE = "type"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD = "payload"
        private const val DB_EVENT_PAYLOAD_SESSION_STATUS = "sessionIsClosed"
    }
}
