package com.simprints.id.data.db.event.local

import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_V1
import com.simprints.id.tools.extensions.getStringWithColumnName
import org.json.JSONObject
import timber.log.Timber

/**
 * The 2021.1.0 (room v2) introduced:
 * 1) Fingenrprint/Face templates format in Fingerprint/FaceApi capture and EnrolmentCreationApi:
 * not a breaking changes for domain/db. Templates format is added in the transformation in the API classes.
 * 2) EnrolmentEvent merged with EnrolmentCreationEvent:
 * Added a new class EnrolmentEventV2. The original EnrolmentEvent is now called EnrolmentEventV1.
 * To serialise/deserialize the events stored from previous versions of SID, they need to
 * have ENROLMENT_V1 as type, instead of ENROLMENT. Migration required.
 */
class EventMigration1to2(val crashReportManager: CrashReportManager) : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Timber.d("Migrating from 1 to 2.")
            val enrolmentEvents = database.query("SELECT * from DbEvent where type = 'ENROLMENT'")
            enrolmentEvents.use {
                while (it.moveToNext()) {
                    val id = it.getStringWithColumnName("id")
                    migrateEnrolmentEventType(id, database)
                    migrateEnrolmentEventPayloadType(it, database, id)
                }
            }
            Timber.d("Migration 1 to 2 done")
        } catch (t: Throwable) {
            crashReportManager.logException(t)
            Timber.d(t)
        }
    }

    private fun migrateEnrolmentEventPayloadType(it: Cursor, database: SupportSQLiteDatabase, id: String?) {
        val jsonData = it.getStringWithColumnName("eventJson")
        jsonData?.let {
            val originalJson = JSONObject(jsonData).put("type", ENROLMENT_V1)
            val newPayload = originalJson.getJSONObject("payload").put("type", ENROLMENT_V1)
            val newJson = originalJson.put("payload", newPayload)
            database.execSQL("UPDATE DbEvent set eventJson = ? where id = ?", arrayOf(newJson, id))
        }
    }

    private fun migrateEnrolmentEventType(id: String?, database: SupportSQLiteDatabase) {
        id?.let {
            database.execSQL("UPDATE DbEvent set type = ? where id = ?", arrayOf(ENROLMENT_V1, it))
        }
    }
}
