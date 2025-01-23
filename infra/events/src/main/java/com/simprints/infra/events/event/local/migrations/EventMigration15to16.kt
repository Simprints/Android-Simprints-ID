package com.simprints.infra.events.event.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber

internal class EventMigration15to16 : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Simber.i("Migrating room db from schema 15 to schema 16.", tag = MIGRATION)
        renameFaceLicenseErrors(db)
        Simber.i("Migration from schema 15 to schema 16 done.", tag = MIGRATION)
    }

    // rename FACE_LICENSE_MISSING and FACE_LICENSE_INVALID to LICENSE_MISSING and LICENSE_INVALID
    private fun renameFaceLicenseErrors(database: SupportSQLiteDatabase) {
        // select all alert AlertScreenEvent with type FACE_LICENSE_MISSING or FACE_LICENSE_INVALID
        val cursor = database.query(
            "SELECT * FROM $TABLE_NAME WHERE $EVENT_TYPE =?",
            arrayOf(ALERT_EVENT_TYPE),
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName("id")
                val jsonData = it.getStringWithColumnName(DB_EVENT_JSON_FIELD)
                // only update the json if it contains FACE_LICENSE_MISSING or FACE_LICENSE_INVALID
                if (jsonData?.contains(FACE_LICENSE_MISSING) == true ||
                    jsonData?.contains(
                        FACE_LICENSE_INVALID,
                    ) == true
                ) {
                    jsonData.let {
                        val updatedJson = jsonData
                            .replace(
                                "\"$FACE_LICENSE_MISSING\"",
                                "\"$LICENSE_MISSING\"",
                            ).replace(
                                "\"$FACE_LICENSE_INVALID\"",
                                "\"$LICENSE_INVALID\"",
                            )
                        database.execSQL(
                            "UPDATE $TABLE_NAME SET $DB_EVENT_JSON_FIELD = ? WHERE id = ?",
                            arrayOf(updatedJson, id),
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val TABLE_NAME = "DbEvent"
        const val DB_EVENT_JSON_FIELD = "eventJson"
        const val EVENT_TYPE = "type"
        const val ALERT_EVENT_TYPE = "ALERT_SCREEN"
        const val FACE_LICENSE_MISSING = "FACE_LICENSE_MISSING"
        const val LICENSE_MISSING = "LICENSE_MISSING"
        const val FACE_LICENSE_INVALID = "FACE_LICENSE_INVALID"
        const val LICENSE_INVALID = "LICENSE_INVALID"
    }
}
