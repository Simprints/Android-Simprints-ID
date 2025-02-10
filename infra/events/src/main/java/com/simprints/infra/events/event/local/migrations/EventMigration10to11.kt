package com.simprints.infra.events.event.local.migrations

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.Keep
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.tools.extentions.getStringWithColumnName
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.events.event.domain.models.scope.DatabaseInfo
import com.simprints.infra.events.event.domain.models.scope.Device
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopePayload
import com.simprints.infra.events.event.domain.models.scope.Location
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MIGRATION
import com.simprints.infra.logging.Simber

internal class EventMigration10to11 : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Simber.i("Migrating room db from schema 10 to schema 11.", tag = MIGRATION)

        createSessionScopeTable(db)
        convertAllSessionCaptureEventsToScopes(db)
        deleteSessionCaptureEvents(db)
        deleteArtificialTerminationEvents(db)

        Simber.i("Migration from schema 10 to schema 11 done.", tag = MIGRATION)
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
            """.trimIndent(),
        )
    }

    private fun convertAllSessionCaptureEventsToScopes(database: SupportSQLiteDatabase) {
        val cursor = database.query(
            "SELECT * FROM $EVENT_TABLE_NAME WHERE type = ?",
            arrayOf(KEY_EVENT_TYPE_SESSION_CAPTURE),
        )
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getStringWithColumnName("id")
                val scope = getScopeFromCursor(it)
                if (scope == null) {
                    Simber.d("Could not parse session scope from event with id $id", tag = MIGRATION)
                    continue
                }
                database.insert(SCOPE_TABLE_NAME, SQLiteDatabase.CONFLICT_NONE, scope)
            }
        }
    }

    private fun getScopeFromCursor(it: Cursor): ContentValues? {
        val jsonData = it.getStringWithColumnName("eventJson") ?: return null
        val event = fromJsonToDomain(jsonData)
        val sessionId = event.labels.sessionId ?: return null

        val endedAt = event.payload.endedAt.takeIf { it > 0 }

        val payloadJson = JsonHelper.toJson(
            EventScopePayload(
                // Other end causes have not been used for a long time so it is save to assume
                // that all previous sessions ended with new session termination cause
                endCause = if (endedAt != null) EventScopeEndCause.NEW_SESSION else null,
                sidVersion = event.payload.appVersionName,
                libSimprintsVersion = event.payload.libVersionName,
                language = event.payload.language,
                projectConfigurationUpdatedAt = "",
                modalities = event.payload.modalities,
                device = event.payload.device,
                databaseInfo = event.payload.databaseInfo,
                location = event.payload.location,
            ),
        )

        return ContentValues().apply {
            put("id", sessionId)
            put("projectId", event.payload.projectId)
            put("createdAt", event.payload.createdAt)
            if (endedAt != null) {
                put("endedAt", endedAt)
            }
            put("payloadJson", payloadJson)
        }
    }

    private fun fromJsonToDomain(eventJson: String): OldSessionCaptureEvent = JsonHelper.fromJson(
        json = eventJson,
        type = object : TypeReference<OldSessionCaptureEvent>() {},
    )

    private fun deleteSessionCaptureEvents(database: SupportSQLiteDatabase) {
        database.execSQL(
            "DELETE FROM DbEvent WHERE type = ?",
            arrayOf(KEY_EVENT_TYPE_SESSION_CAPTURE),
        )
    }

    private fun deleteArtificialTerminationEvents(database: SupportSQLiteDatabase) {
        database.execSQL(
            "DELETE FROM DbEvent WHERE type = ?",
            arrayOf(KEY_EVENT_TYPE_TERMINATION),
        )
    }

    companion object {
        private const val SCOPE_TABLE_NAME = "DbSessionScope"
        private const val EVENT_TABLE_NAME = "DbEvent"

        private const val KEY_EVENT_TYPE_SESSION_CAPTURE = "SESSION_CAPTURE"
        private const val KEY_EVENT_TYPE_TERMINATION = "ARTIFICIAL_TERMINATION"
    }

    // Snapshot of the session capture event structure at the moment when this migration was
    // introduced. This is needed to be able to parse the old events from the database.
    @Keep
    internal data class OldSessionCaptureEvent(
        val id: String,
        var labels: EventLabels,
        val payload: SessionCapturePayload,
    ) {
        @Keep
        data class EventLabels(
            val sessionId: String? = null,
        )

        @Keep
        data class SessionCapturePayload(
            var projectId: String,
            val createdAt: Long,
            var modalities: List<GeneralConfiguration.Modality>,
            val appVersionName: String,
            val libVersionName: String,
            var language: String,
            val device: Device,
            val databaseInfo: DatabaseInfo,
            var location: Location? = null,
            var endedAt: Long = 0,
        )
    }
}
