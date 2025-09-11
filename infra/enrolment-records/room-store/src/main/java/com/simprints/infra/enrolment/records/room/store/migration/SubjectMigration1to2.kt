package com.simprints.infra.enrolment.records.room.store.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

/**
 * Schema version 1 -> 2
 *
 * Changes:
 * - Adding [DbExternalCredential] entity
 * */
@ExcludedFromGeneratedTestCoverageReports("Covered indirectly in the migration tests")
class SubjectMigration1to2 : Migration(1, 2) {
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
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_DbExternalCredential_subjectId` ON `DbExternalCredential` (`subjectId`)")
    }
}

