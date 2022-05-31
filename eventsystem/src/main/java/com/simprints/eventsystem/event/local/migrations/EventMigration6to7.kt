package com.simprints.eventsystem.event.local.migrations

import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.logging.Simber
import org.json.JSONObject

/**
 *
 * Changes OneToOneMatchEvent by adding fingerComparisonStrategy to all fingerprint matching events
 */
class EventMigration6to7 : Migration(6, 7) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Simber.d("Migrating room db from schema 6 to schema 7.")
            migrateOneToOneMatchEvents(database)
            Simber.d("Migration from schema 5 to schema 6 done.")
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    private fun migrateOneToOneMatchEvents(database: SupportSQLiteDatabase) {
        val eventsQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ?", arrayOf("ONE_TO_ONE_MATCH")
        )
        eventsQuery.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName("id")
                migrateOneToOneMatchEvent(it, database, id)
            }
        }
    }

    /**
     *  Migrate OneToOneMatchEvent to v2 and add fingerComparisonStrategy to fingerprint matching events
     */
    private fun migrateOneToOneMatchEvent(
        it: Cursor,
        database: SupportSQLiteDatabase,
        id: String?
    ) {
        val jsonData = it.getStringWithColumnName(DB_EVENT_JSON_FIELD)
        jsonData?.let {
            val originalJson = JSONObject(jsonData)
            val newPayload = originalJson.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)
            newPayload.put(VERSION_PAYLOAD_NAME, NEW_EVENT_VERSION_VALUE)
            // if the matching algorithm is SIM_AFIS we add SAME_FINGER fingerComparisonStrategy
            if (newPayload.getString(MATCHER) == SIM_AFIS) {
                newPayload.put(FINGER_COMPARISON_STRATEGY, SAME_FINGER)
            }
            val newJson = originalJson.put(DB_EVENT_JSON_EVENT_PAYLOAD, newPayload)
            database.execSQL(
                "UPDATE DbEvent SET eventJson = ? WHERE id = ?",
                arrayOf(newJson, id)
            )


        }
    }

    companion object {
        private const val DB_EVENT_JSON_FIELD = "eventJson"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD = "payload"
        private const val MATCHER = "matcher"
        private const val SIM_AFIS = "SIM_AFIS"
        private const val FINGER_COMPARISON_STRATEGY = "fingerComparisonStrategy"
        private const val SAME_FINGER = "SAME_FINGER"
        private const val VERSION_PAYLOAD_NAME = "eventVersion"
        private const val NEW_EVENT_VERSION_VALUE = 2

    }


}
