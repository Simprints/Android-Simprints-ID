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
 * In the 2021.1.0 (room v2) was introduced:
 * 1) Fingenrprint/Face templates format in Fingerprint/Face capture and EnrolmentCreation API:
 * not a breaking changes for domain/db. In the transformation to API, the templates formats will have a default values.
 * 2) EnrolmentEvent_V2 introduced: a merge of EnrolmentCreationEvent and EnrolmentEvent_v1:
 * So the old EnrolmentEvents need to have the type EnrolmentEvent_v1 - migration required
 */
class EventMigration1to2(val crashReportManager: CrashReportManager) : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Timber.d("Migrating from 1 to 2.")
            val enrolmentEvents = database.query("SELECT * from DbEvent where type = 'ENROLMENT'")
            enrolmentEvents.use {
                while (it.moveToNext()) {
                    val id = it.getStringWithColumnName("id")
                    val jsonData = it.getStringWithColumnName("eventJson")
                    jsonData?.let {
                        val originalJson = JSONObject(jsonData)
                        val newPayload = originalJson.getJSONObject("payload").put("type", ENROLMENT_V1)
                        val newJson = originalJson.put("payload", newPayload)
                        database.execSQL("UPDATE DbEvent set eventJson = ? where id = ?", arrayOf(newJson, id))
                        database.execSQL("UPDATE DbEvent set type = ? where id = ?", arrayOf(ENROLMENT_V1, id))
                    }
                }
            }
            Timber.d("Migration 1 to 2 done")
        } catch (t: Throwable) {
            crashReportManager.logException(t)
            Timber.d(t)
        }
    }
}
