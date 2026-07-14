package com.simprints.infra.enrolment.records.room.store.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

/**
 * Schema version 2 -> 3
 *
 * Changes:
 * - Backfills empty [DbBiometricTemplate.referenceId] values caused by a missing Realm migration.
 *   Each distinct (subjectId, modality) group with an empty referenceId is assigned a new UUID so
 *   that all templates from the same original capture session share the same reference ID.
 */
class SubjectMigration2to3 : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val cursor = db.query(
            "SELECT DISTINCT subjectId, modality FROM DbBiometricTemplate WHERE referenceId = ''",
        )
        cursor.use {
            while (cursor.moveToNext()) {
                val subjectId = cursor.getString(0)
                val modality = cursor.getInt(1)
                val newReferenceId = UUID.randomUUID().toString()
                db.execSQL(
                    "UPDATE DbBiometricTemplate SET referenceId = ? WHERE subjectId = ? AND modality = ? AND referenceId = ''",
                    arrayOf(newReferenceId, subjectId, modality),
                )
            }
        }
    }
}
