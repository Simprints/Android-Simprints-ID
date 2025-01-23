package com.simprints.infra.events.event.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber

/**
 * Migration from 2021.1.0 to 2021.1.1. This migration adds a new column that can mark an event as
 * sessionIsClosed. This column is only relevant the the SESSION_CAPTURE event and it allows us to
 * query and close open sessions instead of reading all sessions and checking their is closed field
 * in the JSON payload.
 */
internal class EventMigration2to3 : Migration(2, 3) {
    private val sessionType = "SESSION_CAPTURE"
    private val idColumn = "id"

    override fun migrate(database: SupportSQLiteDatabase) {
        Simber.i("Migrating room db from schema 2 to schema 3.", tag = MIGRATION)
        try {
            /**
             * Update the table to include a sessionIsClosed column and set it's default to false.
             */
            addNewSessionIsClosedColumn(database)

            /**
             * Go through the DB and mark sessions that have an endTime as closed. In 2021.1.0 the
             * end time is only added when a session is closed. The rest of the sessions (should
             * only be 1) will be closed the first time the user opens the app after the migration.
             */
            updateTableToCloseClosedSessions(database)
            Simber.i("Migration from schema 2 to schema 3 done.", tag = MIGRATION)
        } catch (ex: Exception) {
            Simber.e("Failed to migrate room db from schema 2 to schema 3.", ex, tag = MIGRATION)
        }
    }

    private fun addNewSessionIsClosedColumn(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE DbEvent ADD COLUMN sessionIsClosed INTEGER NOT NULL DEFAULT(0)")
    }

    private fun updateTableToCloseClosedSessions(database: SupportSQLiteDatabase) {
        val enrolmentEventsQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ? AND endedAt != 0",
            arrayOf(sessionType),
        )

        enrolmentEventsQuery.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName(idColumn) ?: return
                database.execSQL("UPDATE DbEvent SET sessionIsClosed = 1 WHERE id = ?", arrayOf(id))
            }
        }
    }
}
