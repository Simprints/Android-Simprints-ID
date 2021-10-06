package com.simprints.eventsystem.event.local.migrations

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationTestingTools {

    fun retrieveCursorWithEventById(db: SupportSQLiteDatabase, id: String): Cursor =
        db.query("SELECT * from DbEvent where id= ?", arrayOf(id)).apply { moveToNext() }

}
