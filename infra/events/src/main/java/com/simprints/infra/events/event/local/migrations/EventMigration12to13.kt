package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getLongWithColumnName
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber

internal class EventMigration12to13 : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Simber.i("Migrating room db from schema 12 to schema 13.", tag = MIGRATION)
        migrateEventJson(db)
        Simber.i("Migration from schema 12 to schema 13 done.", tag = MIGRATION)
    }

    private fun migrateEventJson(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `${TEMP_EVENT_TABLE_NAME}` (
                `id` TEXT NOT NULL,
                `createdAt_unixMs` INTEGER NOT NULL,
                `createdAt_isTrustworthy` INTEGER NOT NULL,
                `createdAt_msSinceBoot` INTEGER,
                `type` TEXT NOT NULL,
                `projectId` TEXT,
                `sessionId` TEXT,
                `eventJson` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )

        // Get all events
        val eventsQuery = database.query("SELECT * FROM $EVENT_TABLE_NAME")
        eventsQuery.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getStringWithColumnName("id") ?: break
                val type = cursor.getStringWithColumnName("type") ?: break
                val createdAt = cursor.getLongWithColumnName("createdAt") ?: break
                val jsonData = cursor.getStringWithColumnName("eventJson") ?: break
                val sessionId = cursor.getStringWithColumnName("sessionId")
                val projectId = cursor.getStringWithColumnName("projectId")

                database.insert(
                    TEMP_EVENT_TABLE_NAME,
                    SQLiteDatabase.CONFLICT_REPLACE,
                    ContentValues().apply {
                        put("id", id)
                        put("createdAt_unixMs", createdAt)
                        put("createdAt_isTrustworthy", 0)
                        put("type", type)
                        put("projectId", projectId)
                        put("sessionId", sessionId)
                        put("eventJson", convertEventJson(jsonData))
                    },
                )
            }
        }

        // delete the old table
        database.execSQL("DROP TABLE $EVENT_TABLE_NAME")
        // rename new table to previous one
        database.execSQL("ALTER TABLE $TEMP_EVENT_TABLE_NAME RENAME TO $EVENT_TABLE_NAME")
    }

    @VisibleForTesting(VisibleForTesting.PRIVATE)
    internal fun convertEventJson(oldEvent: String) = oldEvent
        .replace(labelsRegex, labelsReplacement)
        .replace(createdAtRegex, createdAtReplacement)
        .replace(endedAtRegex) { matchResult ->
            if (matchResult.groupValues[1] == "0") {
                "\"endedAt\":null"
            } else {
                "\"endedAt\":{\"ms\":${matchResult.groupValues[1]},\"isTrustworthy\":false,\"msSinceBoot\":null}"
            }
        }.replace(versionRegex) { matchResult ->
            "\"eventVersion\":${matchResult.groupValues[1].toInt().plus(1)}"
        }

    private val labelsRegex =
        "\"labels\":\\{(\"projectId\":\".*?\",\"sessionId\":\".*?\").*?\\}".toRegex()
    private val labelsReplacement = "$1"

    private val createdAtRegex = "\"createdAt\":(\\d+)".toRegex()
    private val createdAtReplacement =
        "\"createdAt\":{\"ms\":$1,\"isTrustworthy\":false,\"msSinceBoot\":null}"

    private val endedAtRegex = "\"endedAt\":(\\d+)".toRegex()
    private val versionRegex = "\"eventVersion\":(\\d+)".toRegex()

    companion object {
        private const val EVENT_TABLE_NAME = "DbEvent"
        private const val TEMP_EVENT_TABLE_NAME = "DbEvent_temp"
    }
}
