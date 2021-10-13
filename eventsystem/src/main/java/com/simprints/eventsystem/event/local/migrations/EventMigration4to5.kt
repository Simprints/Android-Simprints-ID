package com.simprints.eventsystem.event.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * This migration updates EventLabels payload, removing the parameter subjectId, as it is no longer
 * being used.
 */
class EventMigration4to5: Migration(4, 5)  {

    override fun migrate(database: SupportSQLiteDatabase) {
        // left empty because nothing to do here
    }
}
