package com.simprints.infra.events.event.local.migrations

import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.events.event.domain.models.session.SessionEndCause
import com.simprints.infra.events.event.domain.models.session.SessionScope
import com.simprints.infra.events.event.domain.models.session.SessionScopePayload
import com.simprints.infra.events.event.local.models.DbEvent
import com.simprints.infra.logging.Simber

internal class EventMigration10to11 : Migration(10, 11) {

    override fun migrate(db: SupportSQLiteDatabase) {
        Simber.d("Migrating room db from schema 10 to schema 11.")

        createSessionScopeTable(db)
        convertAllSessionCaptureEventsToScopes(db)
        deleteSessionCaptureEvents(db)
        deleteArtificialTerminationEvents(db)

        Simber.d("Migration from schema 10 to schema 11 done.")
    }

    private fun createSessionScopeTable(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `$SCOPE_TABLE_NAME` (
                    `id` TEXT NOT NULL,
                    `projectId` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `endedAt` INTEGER,
                    `payloadJson` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
            """.trimIndent()
        )
    }

    private fun convertAllSessionCaptureEventsToScopes(database: SupportSQLiteDatabase) {
        val cursor = database.query(
            "SELECT * FROM $EVENT_TABLE_NAME WHERE type = ?",
            arrayOf(KEY_EVENT_TYPE_SESSION_CAPTURE)
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName("id")
                val scope = getScopeFromCursor(it)
                if (scope == null) {
                    Simber.d("Could not parse session scope from event with id $id")
                    continue
                }
                writeSessionScopeToDatabase(database, scope)
            }
        }
    }

    private fun getScopeFromCursor(it: Cursor): SessionScope? {
        val jsonData = it.getStringWithColumnName("eventJson") ?: return null
        val event = fromJsonToDomain(jsonData)
        val sessionId = event.labels.sessionId ?: return null

        val endedAt = event.payload.endedAt.takeIf { it > 0 }?.let { Timestamp.fromLong(it) }

        return SessionScope(
            id = sessionId,
            projectId = event.payload.projectId,
            createdAt = Timestamp.fromLong(event.payload.createdAt),
            endedAt = endedAt,
            payload = SessionScopePayload(
                // Other end causes have not been used for a long time so it is save to assume
                // that all previous sessions ended with new session termination cause
                endCause = if (endedAt != null) SessionEndCause.NEW_SESSION else null,
                sidVersion = event.payload.appVersionName,
                libSimprintsVersion = event.payload.libVersionName,
                language = event.payload.language,
                projectConfigurationUpdatedAt = "",
                modalities = event.payload.modalities,
                device = event.payload.device,
                databaseInfo = event.payload.databaseInfo,
                location = event.payload.location,
            )
        )
    }

    private fun fromJsonToDomain(eventJson: String): SessionCaptureEvent = JsonHelper.fromJson(
        json = eventJson,
        module = DbEvent.dbSerializationModule,
        type = object : TypeReference<SessionCaptureEvent>() {}
    )

    private fun writeSessionScopeToDatabase(database: SupportSQLiteDatabase, scope: SessionScope) {
        val payloadJson = JsonHelper.toJson(scope.payload)
        database.compileStatement(
            """
                INSERT INTO $SCOPE_TABLE_NAME (id, projectId, createdAt, endedAt, payloadJson) 
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent()
        ).use {
            it.bindString(1, scope.id)
            it.bindString(2, scope.projectId)
            it.bindLong(3, scope.createdAt.ms)
            if (scope.endedAt != null) {
                it.bindLong(4, scope.endedAt!!.ms)
            } else {
                it.bindNull(4)
            }
            it.bindString(5, payloadJson)
            it.executeInsert()
        }
    }

    private fun deleteSessionCaptureEvents(database: SupportSQLiteDatabase) {
        database.execSQL(
            "DELETE FROM DbEvent WHERE type = ?",
            arrayOf(KEY_EVENT_TYPE_SESSION_CAPTURE)
        )

    }

    private fun deleteArtificialTerminationEvents(database: SupportSQLiteDatabase) {
        database.execSQL(
            "DELETE FROM DbEvent WHERE type = ?",
            arrayOf(KEY_EVENT_TYPE_TERMINATION)
        )
    }

    companion object {

        private const val SCOPE_TABLE_NAME = "DbSessionScope"
        private const val EVENT_TABLE_NAME = "DbEvent"

        private const val KEY_EVENT_TYPE_SESSION_CAPTURE = "SESSION_CAPTURE"
        private const val KEY_EVENT_TYPE_TERMINATION = "ARTIFICIAL_TERMINATION"

    }
}

