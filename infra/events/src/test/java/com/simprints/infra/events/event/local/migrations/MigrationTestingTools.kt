package com.simprints.infra.events.event.local.migrations

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationTestingTools {
    fun retrieveCursorWithEventById(
        db: SupportSQLiteDatabase,
        id: String,
    ): Cursor = db.query("SELECT * from DbEvent where id= ?", arrayOf(id)).apply { moveToNext() }

    fun retrieveCursorWithEventByType(
        db: SupportSQLiteDatabase,
        type: String,
    ): Cursor = db.query("SELECT * from DbEvent where type= ?", arrayOf(type)).apply { moveToNext() }

    /**
     * Only helpful in 11-13 migrations, in version 14 DbSessionScope
     * table has been renamed to DbEventScope
     */
    fun retrieveCursorWithSessionScopeById(
        db: SupportSQLiteDatabase,
        id: String,
    ): Cursor = db.query("SELECT * from DbSessionScope where id= ?", arrayOf(id)).apply { moveToNext() }

    fun retrieveCursorWithEventScopeById(
        db: SupportSQLiteDatabase,
        id: String,
    ): Cursor = db.query("SELECT * from DbEventScope where id= ?", arrayOf(id)).apply { moveToNext() }
}
