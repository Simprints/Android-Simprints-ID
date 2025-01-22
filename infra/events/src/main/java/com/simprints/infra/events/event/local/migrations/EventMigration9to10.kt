package com.simprints.infra.events.event.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.domain.tokenization.serialization.FIELD_CLASS_NAME
import com.simprints.core.domain.tokenization.serialization.FIELD_VALUE
import com.simprints.core.domain.tokenization.serialization.RAW
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber

/**
 * [CORE-2502]
 * This migration aims to reflect changes introduced with the tokenization (encryption) of the
 * 'moduleId' and 'attendantId' fields in the events
 *
 * Starting from the database version 10, ID fields above need to be stored in encrypted manner.
 * However, in order to properly encrypt the previously stored entities, it is necessary to
 * differentiate between the raw values and the values that were already encrypted. Since there is
 * no reliable way to do so, the TokenizableString sealed class was introduced that explicitly
 * specifies the encryption status of the stored value.
 *
 *    This migration changes the
 *      | "field": "some_id"
 *    to the
 *      | "field": { "className": "TokenizableString.Raw", "value": "some_id" }
 *    within the json string stored in DbEvent::eventJson.
 *
 * This way the objects stored previously in the database can be safely casted from the old JSON to
 * the new Event objects containing TokenizableString fields.
 */
internal class EventMigration9to10 : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Simber.i("Migrating room db from schema 9 to schema 10.", tag = MIGRATION)
        migrateEventJson(database)
        Simber.i("Migration from schema 9 to schema 10 done.", tag = MIGRATION)
    }

    private fun migrateEventJson(database: SupportSQLiteDatabase) {
        // Get all events
        val eventsQuery = database.query("SELECT * FROM $DB_EVENT_ENTITY")
        eventsQuery.use { cursor ->
            while (cursor.moveToNext()) {
                // Getting event JSON string that contains all the event fields
                val id = cursor.getStringWithColumnName("id") ?: break
                val jsonData = cursor.getStringWithColumnName(DB_EVENT_JSON_FIELD) ?: break

                // Updating JSON attendantId/moduleId fields with new values that represent TokenizedString
                val migratedJson =
                    jsonData.migrateJsonStringToTokenizableString(USER_ID, ATTENDANT_ID, MODULE_ID)
                database.execSQL(
                    "UPDATE $DB_EVENT_ENTITY SET $DB_EVENT_JSON_FIELD = ? WHERE id = ?",
                    arrayOf(migratedJson, id),
                )
            }
        }
    }

    companion object {
        private const val DB_EVENT_ENTITY = "DbEvent"
        private const val DB_EVENT_JSON_FIELD = "eventJson"
        private const val ATTENDANT_ID = "attendantId"
        private const val USER_ID = "userId"
        private const val MODULE_ID = "moduleId"
    }

    internal fun String.migrateJsonStringToTokenizableString(vararg fieldsToMigrate: String): String =
        fieldsToMigrate.fold(this) { json, field ->
            val modifiedJson = StringBuilder(json)
            val searchField = "\"$field\":\""
            val classNameValue = RAW

            val startIndex = modifiedJson.indexOf(searchField)

            if (startIndex != -1) {
                val endIndex = modifiedJson.indexOf("\"", startIndex + searchField.length)
                if (endIndex != -1) {
                    val userIdValue =
                        modifiedJson.substring(startIndex + searchField.length, endIndex)
                    val replacement =
                        "\"$field\":{\"$FIELD_CLASS_NAME\":\"$classNameValue\",\"$FIELD_VALUE\":\"$userIdValue\"}"
                    modifiedJson.replace(startIndex, endIndex + 1, replacement)
                }
            }

            return@fold modifiedJson.toString()
        }
}
