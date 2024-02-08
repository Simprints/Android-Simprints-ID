package com.simprints.infra.events.event.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.infra.logging.Simber

internal class EventMigration13to14 : Migration(13, 14) {

    override fun migrate(database: SupportSQLiteDatabase) {
        Simber.d("Migrating room db from schema 11 to schema 12.")
        updateSessionScopesToEventScopes(database)
        Simber.d("Migration from schema 11 to schema 12 done.")
    }

    private fun updateSessionScopesToEventScopes(database: SupportSQLiteDatabase) {
        // create the new table with correct columns
        database.execSQL(
            """CREATE TABLE `$EVENT_SCOPE_TABLE_NAME` (
                |`id` TEXT NOT NULL, 
                |`projectId` TEXT NOT NULL,
                |`type` TEXT NOT NULL,
                |`start_unixMs` INTEGER NOT NULL,
                |`start_isTrustworthy` INTEGER NOT NULL,
                |`start_msSinceBoot` INTEGER,
                |`end_unixMs` INTEGER,
                |`end_isTrustworthy` INTEGER,
                |`end_msSinceBoot` INTEGER,
                |`payloadJson` TEXT NOT NULL,
                |PRIMARY KEY(`id`))""".trimMargin()
        )

        // copy existing data into new table
        database.execSQL(
            "INSERT INTO $EVENT_SCOPE_TABLE_NAME ($EVENT_SCOPE_COLUMNS) SELECT $SESSION_SCOPE_OLD_COLUMNS FROM $SESSION_SCOPE_TABLE_NAME"
        )

        // delete the old table
        database.execSQL("DROP TABLE $SESSION_SCOPE_TABLE_NAME")
    }

    companion object {

        private const val EVENT_SCOPE_TABLE_NAME = "DbEventScope"
        private const val SESSION_SCOPE_TABLE_NAME = "DbSessionScope"

        private const val EVENT_SCOPE_COLUMNS =
            "id, projectId, type, start_unixMs, start_isTrustworthy, start_msSinceBoot, end_unixMs, end_isTrustworthy, end_msSinceBoot, payloadJson"
        private const val SESSION_SCOPE_OLD_COLUMNS =
            "id, projectId, 'SESSION', start_unixMs, start_isTrustworthy, start_msSinceBoot, end_unixMs, end_isTrustworthy, end_msSinceBoot, payloadJson"

    }
}

