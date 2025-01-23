package com.simprints.infra.events.event.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber

/**
 * This migration updates EventLabels payload, removing the parameter subjectId, as it is no longer
 * being used.
 */
internal class EventMigration4to5 : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Simber.i("Migrating room db from schema 4 to schema 5.", tag = MIGRATION)
        removeSubjectIdColumn(database)
        Simber.i("Migration from schema 4 to schema 5 done.", tag = MIGRATION)
    }

    private fun removeSubjectIdColumn(database: SupportSQLiteDatabase) {
        // create the new table, without 'subjectId' column
        database.execSQL(
            """CREATE TABLE `$TEMP_TABLE_NAME` (
                |`id` TEXT NOT NULL, 
                |`type` TEXT, 
                |`eventJson` TEXT NOT NULL, 
                |`createdAt` INTEGER NOT NULL, 
                |`endedAt` INTEGER NOT NULL, 
                |`sessionIsClosed` INTEGER NOT NULL, 
                |`projectId` TEXT,
                |`attendantId` TEXT, 
                |`moduleIds` TEXT, 
                |`mode` TEXT, 
                |`sessionId` TEXT, 
                |`deviceId` TEXT, 
                |PRIMARY KEY(`id`))
            """.trimMargin(),
        )

        // copy existing data into new table
        database.execSQL(
            "INSERT INTO $TEMP_TABLE_NAME ($COLUMNS) SELECT $COLUMNS FROM $TABLE_NAME",
        )

        // delete the old table
        database.execSQL("DROP TABLE $TABLE_NAME")
        // rename new table to previous one
        database.execSQL("ALTER TABLE $TEMP_TABLE_NAME RENAME TO $TABLE_NAME")
    }

    companion object {
        private const val TEMP_TABLE_NAME = "DbEvent_Temp"
        private const val TABLE_NAME = "DbEvent"
        private const val COLUMNS = "`id`, `type`, `eventJson`, `createdAt`, `endedAt`, " +
            "`sessionIsClosed`, `projectId`, `attendantId`, `moduleIds`, `mode`, `sessionId`, `deviceId`"
    }
}
