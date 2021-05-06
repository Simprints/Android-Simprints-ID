package com.simprints.id.data.db.event.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.tools.extensions.getStringWithColumnName
import com.simprints.id.tools.time.TimeHelper
import org.json.JSONObject
import java.util.*

/**
 * Migration from 2021.1.0 to 2021.1.1. This migration adds a new column that can mark an event as
 * sessionIsClosed. This column is only relevant the the SESSION_CAPTURE event and it allows us to
 * query and close open sessions instead of reading all sessions and checking their is closed field
 * in the JSON payload.
 */
class EventMigration2to3(val crashReportManager: CrashReportManager, timeHelper: TimeHelper) :
    Migration(2, 3) {

    private val now = timeHelper.now()

    private val sessionType = "SESSION_CAPTURE"
    private val terminationType = "ARTIFICIAL_TERMINATION"
    private val idColumn = "id"
    private val eventJsonColumn = "eventJson"
    private val payloadJsonName = "payload"
    private val labelsJsonName = "labels"
    private val endedAtName = "endedAt"
    private val projectIdName = "projectId"

    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            /**
             * Update the table to include a sessionIsClosed column and set it's default to false
             * Them update the column so all values are changed to true since we need to close any
             * open sessions before the migration.
             */
            addNewSessionIsClosedColumn(database)

            /**
             * If there were open sessions before the migration we need to add their closed fields and
             * create an artificial termination event for them.
             */
            closeLatestSessionIfNotClosed(database)
        } catch (ex: Exception) {
            crashReportManager.logException(ex)
            throw ex
        }
    }

    private fun addNewSessionIsClosedColumn(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE DbEvent ADD COLUMN sessionIsClosed INTEGER NOT NULL DEFAULT(0)")
        database.execSQL("UPDATE DbEvent SET sessionIsClosed = 1")
    }

    private fun closeLatestSessionIfNotClosed(database: SupportSQLiteDatabase) {
        val latestSessionCursor = database.query(
            "SELECT * FROM DbEvent WHERE type = ? AND endedAt = 0",
            arrayOf(sessionType)
        )

        latestSessionCursor.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName(idColumn) ?: return
                val jsonData = it.getStringWithColumnName(eventJsonColumn) ?: return

                val sessionJson = JSONObject(jsonData)
                updateSessionEndedAt(database, id, sessionJson)

                val labels = sessionJson.getJSONObject(labelsJsonName)
                createArtificialTerminationEvent(database, labels)
            }
        }
    }

    private fun updateSessionEndedAt(
        database: SupportSQLiteDatabase,
        id: String,
        originalJson: JSONObject
    ) {
        val newPayload =
            originalJson.getJSONObject(payloadJsonName).put(endedAtName, now)
        val newJson = originalJson.put(payloadJsonName, newPayload)

        database.execSQL(
            "UPDATE DbEvent SET endedAt = ?, eventJson = ? WHERE id = ?",
            arrayOf(now, newJson, id)
        )
    }

    private fun createArtificialTerminationEvent(
        database: SupportSQLiteDatabase,
        labels: JSONObject
    ) {
        val termId = UUID.randomUUID().toString()
        val termJson =
            getTerminationWithoutLabelsJson(termId).apply { this.put(labelsJsonName, labels) }

        database.execSQL(
            "INSERT INTO DbEvent (id, type, eventJson, createdAt, endedAt, sessionIsClosed, projectId) VALUES(?,?,?,?,?,?,?)",
            arrayOf(termId, terminationType, termJson, now, 0, 1, labels.get(projectIdName))
        )
    }

    private fun getTerminationWithoutLabelsJson(id: String): JSONObject = JSONObject(
        mapOf(
            Pair("id", id),
            Pair("type", terminationType),
            Pair(
                "payload", mapOf(
                    Pair("eventVersion", 1),
                    Pair("reason", "NEW_SESSION"),
                    Pair("type", terminationType),
                    Pair("createdAt", now),
                    Pair("endedAt", 0)
                )
            )
        )
    )

}
