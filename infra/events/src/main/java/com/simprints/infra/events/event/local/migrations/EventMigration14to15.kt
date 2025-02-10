package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getIntWithColumnName
import com.simprints.core.tools.extentions.getLongWithColumnName
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber
import net.sqlcipher.database.SQLiteDatabase
import org.json.JSONObject

internal class EventMigration14to15 : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Simber.i("Migrating room db from schema 14 to schema 15.", tag = MIGRATION)
        updateSessionIdtoScopeIdColumn(database)
        Simber.i("Migration from schema 14 to schema 15 done.", tag = MIGRATION)
    }

    private fun updateSessionIdtoScopeIdColumn(database: SupportSQLiteDatabase) {
        // create the new table with correct columns
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `${TEMP_EVENT_TABLE_NAME}` (
                    `id` TEXT NOT NULL,
                    `createdAt_unixMs` INTEGER NOT NULL,
                    `createdAt_isTrustworthy` INTEGER NOT NULL,
                    `createdAt_msSinceBoot` INTEGER,
                    `type` TEXT NOT NULL,
                    `projectId` TEXT,
                    `scopeId` TEXT,
                    `eventJson` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
            """.trimMargin(),
        )

        // copy existing data into new table
        val eventsQuery = database.query("SELECT * FROM $TABLE_NAME")
        eventsQuery.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getStringWithColumnName("id") ?: break
                val createdAt = cursor.getLongWithColumnName("createdAt_unixMs") ?: break
                val createdAtTrustworthy =
                    cursor.getIntWithColumnName("createdAt_isTrustworthy") ?: 0
                val createdAtMsSinceBoot = cursor.getIntWithColumnName("createdAt_msSinceBoot")
                val type = cursor.getStringWithColumnName("type") ?: break
                val projectId = cursor.getStringWithColumnName("projectId")
                val sessionId = cursor.getStringWithColumnName("sessionId")
                val jsonData = cursor.getStringWithColumnName("eventJson") ?: break

                val updatedJson = JSONObject(jsonData)
                    .apply {
                        put("scopeId", optString("sessionId"))
                        remove("sessionId")
                    }.toString()

                database.insert(
                    TEMP_EVENT_TABLE_NAME,
                    SQLiteDatabase.CONFLICT_REPLACE,
                    ContentValues().apply {
                        put("id", id)
                        put("createdAt_unixMs", createdAt)
                        put("createdAt_isTrustworthy", createdAtTrustworthy)
                        put("createdAt_msSinceBoot", createdAtMsSinceBoot)
                        put("type", type)
                        put("projectId", projectId)
                        put("scopeId", sessionId)
                        put("eventJson", updatedJson)
                    },
                )
            }
        }

        // delete the old table
        database.execSQL("DROP TABLE $TABLE_NAME")
        // rename new table to previous one
        database.execSQL("ALTER TABLE $TEMP_EVENT_TABLE_NAME RENAME TO $TABLE_NAME")
    }

    companion object {
        private const val TEMP_EVENT_TABLE_NAME = "DbEvent_Temp"
        private const val TABLE_NAME = "DbEvent"
    }
}
