package com.simprints.infra.events.event.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber

/**
 * Adds composite indexes to improve FIFO event upload query performance.
 *
 * - DbEvent(scopeId, createdAt_unixMs): speeds up loadFromScope and loadEventJsonFromScope
 *   which filter by scopeId and sort by creation time for ordered upload.
 * - DbEventScope(type, start_unixMs): speeds up loadClosed which filters by type and
 *   end_unixMs IS NOT NULL then sorts by start_unixMs for ordered upload.
 */
internal class EventMigration17to18 : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Simber.i("Migrating room db from schema 17 to schema 18.", tag = MIGRATION)
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_DbEvent_scopeId_createdAt_unixMs` " +
                "ON `DbEvent` (`scopeId`, `createdAt_unixMs`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_DbEventScope_type_start_unixMs` " +
                "ON `DbEventScope` (`type`, `start_unixMs`)",
        )
        Simber.i("Migration from schema 17 to schema 18 done.", tag = MIGRATION)
    }
}
