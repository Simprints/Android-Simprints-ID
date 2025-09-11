package com.simprints.infra.enrolment.records.room.store.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema version 1 -> 2
 *
 * Changes:
 * - Adding [DbExternalCredential] entity
 * */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `DbExternalCredential` (
                `id` TEXT NOT NULL,
                `value` TEXT NOT NULL,
                `subjectId` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                PRIMARY KEY(`value`, `subjectId`),
                FOREIGN KEY(`subjectId`) REFERENCES `DbSubject`(`subjectId`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }
}

