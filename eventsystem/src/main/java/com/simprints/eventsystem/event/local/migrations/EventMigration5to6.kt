package com.simprints.eventsystem.event.local.migrations

import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.logging.Simber
import org.json.JSONObject

/**
 * https://simprints.atlassian.net/servicedesk/customer/portal/2/RF-170
 * Changes to the SimNetworkUtils to use an enum instead of
 */
class EventMigration5to6 : Migration(5, 6) {

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            Simber.d("Migrating room db from schema 5 to schema 6.")
            migrateConnectivityEvents(database)
            Simber.d("Migration from schema 5 to schema 6 done.")
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    fun migrateConnectivityEvents(database: SupportSQLiteDatabase) {
        val enrolmentEventsQuery = database.query(
            "SELECT * FROM DbEvent WHERE type = ?", arrayOf("CONNECTIVITY_SNAPSHOT")
        )
        enrolmentEventsQuery.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName("id")
                migrateEnrolmentEventPayloadType(it, database, id)
            }
        }
    }

    /**
     * Update the connections array in the payload.
     */
    fun migrateEnrolmentEventPayloadType(
        it: Cursor,
        database: SupportSQLiteDatabase,
        id: String?
    ) {
        val jsonData = it.getStringWithColumnName(DB_EVENT_JSON_FIELD)
        jsonData?.let {
            val originalJson = JSONObject(jsonData)
            val newPayload = originalJson.getJSONObject(DB_EVENT_JSON_EVENT_PAYLOAD)
            val newConnectionsArray = newPayload.getJSONArray(DB_EVENT_JSON_CONNECTIONS_ARRAY)

            for (i in 0 until newConnectionsArray.length()) {
                var item = newConnectionsArray.getJSONObject(i)

                /**
                 * We map mobile and wifi connections to the enum. If it's neither wifi or mobile we
                 * drop it.
                 */
                val type = item.getString(CONNECTIONS_TYPE)
                when {
                    type.lowercase().contains(CONNECTIONS_TYPE_VALUE_MOBILE.lowercase()) -> item.put(
                        CONNECTIONS_TYPE,
                        CONNECTIONS_TYPE_VALUE_MOBILE
                    )
                    type.lowercase().contains(CONNECTIONS_TYPE_VALUE_WIFI.lowercase()) -> item.put(
                        CONNECTIONS_TYPE,
                        CONNECTIONS_TYPE_VALUE_WIFI
                    )
                    else -> item = null

                }

                /**
                 * The current states in BQ are SUSPENDED, BLOCKED, IDLE, CONNECTING, OBTAINING_IPADDR,
                 * DISCONNECTED, CONNECTED. We map everything that isn't connected to disconnected.
                 */
                val state = item?.getString(CONNECTIONS_STATE)
                if (state.equals(CONNECTIONS_STATE_VALUE_CONNECTED))
                    item.put(CONNECTIONS_STATE, CONNECTIONS_STATE_VALUE_CONNECTED)
                else
                    item.put(CONNECTIONS_STATE, CONNECTIONS_STATE_VALUE_DISCONNECTED)

                newConnectionsArray.remove(i)
                item?.let { newConnectionsArray.put(i, item) }
            }

            newPayload.remove(DB_EVENT_JSON_CONNECTIONS_ARRAY)
            newPayload.put(DB_EVENT_JSON_CONNECTIONS_ARRAY, newConnectionsArray)

            val newJson = originalJson.put(DB_EVENT_JSON_EVENT_PAYLOAD, newPayload)
            database.execSQL("UPDATE DbEvent SET eventJson = ? WHERE id = ?", arrayOf(newJson, id))
        }
    }

    companion object {
        private const val DB_EVENT_JSON_FIELD = "eventJson"
        private const val DB_EVENT_JSON_EVENT_PAYLOAD = "payload"
        private const val DB_EVENT_JSON_CONNECTIONS_ARRAY = "connections"
        private const val CONNECTIONS_TYPE = "type"
        private const val CONNECTIONS_STATE = "state"
        private const val CONNECTIONS_TYPE_VALUE_MOBILE = "MOBILE"
        private const val CONNECTIONS_TYPE_VALUE_WIFI = "WIFI"
        private const val CONNECTIONS_STATE_VALUE_CONNECTED = "CONNECTED"
        private const val CONNECTIONS_STATE_VALUE_DISCONNECTED = "DISCONNECTED"
    }


}
