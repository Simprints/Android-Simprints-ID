package com.simprints.infra.events.event.local.migrations

import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import org.json.JSONObject

/**
 * Migration from 2021.1.0 to 2021.3.0. This migrations updates the ConnectivitySnapshotEvent from
 * v1 to v2. In order to accommodate android API 30, we are removing the network type from the event
 * payload.
 */
internal class EventMigration3to4 : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Simber.i("Migrating room db from schema 3 to schema 4.", tag = MIGRATION)
            migrateConnectivityEvents(database)
            Simber.i("Migration from schema 3 to schema 4 done.", tag = MIGRATION)
        } catch (t: Throwable) {
            Simber.e("Failed to migrate room db from schema 3 to schema 4.", t, tag = MIGRATION)
        }
    }

    fun migrateConnectivityEvents(database: SupportSQLiteDatabase) {
        val enrolmentEventsQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ?",
            arrayOf("CONNECTIVITY_SNAPSHOT"),
        )
        enrolmentEventsQuery.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName("id")
                migrateEnrolmentEventPayloadType(it, database, id)
            }
        }
    }

    /**
     * Remove the network type line, and update the version to 2.
     */
    fun migrateEnrolmentEventPayloadType(
        it: Cursor,
        database: SupportSQLiteDatabase,
        id: String?,
    ) {
        val jsonData = it.getStringWithColumnName(DB_EVENT_JSON_FIELD)
        jsonData?.let {
            val originalJson = JSONObject(jsonData)
            val newPayload = originalJson.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)

            newPayload.remove(PAYLOAD_TYPE_NAME)
            newPayload.put(VERSION_PAYLOAD_NAME, NEW_EVENT_VERSION_VALUE)

            val newJson = originalJson.put(DB_EVENT_JSON_EVENT_PAYLOAD, newPayload)
            database.execSQL("UPDATE DbEvent SET eventJson = ? WHERE id = ?", arrayOf(newJson, id))
        }
    }

    companion object {
        private const val DB_EVENT_JSON_FIELD = "eventJson"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD = "payload"
        private const val PAYLOAD_TYPE_NAME = "networkType"
        private const val VERSION_PAYLOAD_NAME = "eventVersion"
        private const val NEW_EVENT_VERSION_VALUE = 2
    }
}
