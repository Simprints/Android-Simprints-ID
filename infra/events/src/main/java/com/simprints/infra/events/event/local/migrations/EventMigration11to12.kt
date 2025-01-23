package com.simprints.infra.events.event.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber

internal class EventMigration11to12 : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Simber.i("Migrating room db from schema 11 to schema 12.", tag = MIGRATION)
        updateTimestampsInSessionScope(database)
        Simber.i("Migration from schema 11 to schema 12 done.", tag = MIGRATION)
    }

    private fun updateTimestampsInSessionScope(database: SupportSQLiteDatabase) {
        // create the new table with correct columns
        database.execSQL(
            """CREATE TABLE `$SCOPE_TEMP_TABLE_NAME` (
                |`id` TEXT NOT NULL, 
                |`projectId` TEXT NOT NULL,
                |`start_unixMs` INTEGER NOT NULL,
                |`start_isTrustworthy` INTEGER NOT NULL,
                |`start_msSinceBoot` INTEGER,
                |`end_unixMs` INTEGER,
                |`end_isTrustworthy` INTEGER,
                |`end_msSinceBoot` INTEGER,
                |`payloadJson` TEXT NOT NULL,
                |PRIMARY KEY(`id`))
            """.trimMargin(),
        )

        // copy existing data into new table
        database.execSQL(
            "INSERT INTO $SCOPE_TEMP_TABLE_NAME ($SCOPE_NEW_COLUMNS) SELECT $SCOPE_OLD_COLUMNS FROM $SCOPE_TABLE_NAME",
        )

        // delete the old table
        database.execSQL("DROP TABLE $SCOPE_TABLE_NAME")
        // rename new table to previous one
        database.execSQL("ALTER TABLE $SCOPE_TEMP_TABLE_NAME RENAME TO $SCOPE_TABLE_NAME")
    }

    companion object {
        private const val SCOPE_TEMP_TABLE_NAME = "DbSession_Temp"
        private const val SCOPE_TABLE_NAME = "DbSessionScope"
        private const val SCOPE_OLD_COLUMNS =
            "`id`, `projectId`, `createdAt`, 0, NULL, `endedAt`, " +
                "CASE WHEN `endedAt` IS NOT NULL THEN 0 ELSE NULL END, " +
                "NULL, `payloadJson`"
        private const val SCOPE_NEW_COLUMNS =
            "`id`, `projectId`, `start_unixMs`, `start_isTrustworthy`, `start_msSinceBoot`, `end_unixMs`, `end_isTrustworthy`, `end_msSinceBoot`, `payloadJson`"
    }
}
