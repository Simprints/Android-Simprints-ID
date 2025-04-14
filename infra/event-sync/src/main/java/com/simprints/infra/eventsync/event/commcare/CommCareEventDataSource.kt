package com.simprints.infra.eventsync.event.commcare

import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.core.net.toUri
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameDeserializer
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameSerializer
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.cosync.CoSyncEnrolmentRecordCreationEventDeserializer
import com.simprints.infra.events.event.cosync.CoSyncEnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.eventsync.status.down.domain.CommCareEventSyncResult
import com.simprints.infra.logging.Simber
import com.simprints.libsimprints.Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal class CommCareEventDataSource @Inject constructor(
    private val jsonHelper: JsonHelper,
    @ApplicationContext private val context: Context,
) {
    suspend fun getEvents(): CommCareEventSyncResult {
        val totalCount = count()
        val eventFlow = loadEnrolmentRecordCreationEventsBySubjectActions()

        return CommCareEventSyncResult(
            totalCount = totalCount,
            eventFlow = eventFlow,
        )
    }

    private suspend fun count(): Int {
        var count = 0
        context.contentResolver
            .query(CASE_METADATA_URI, null, null, null, null)
            ?.use { caseMetadataCursor -> count = caseMetadataCursor.count }
        return count
    }

    private fun loadEnrolmentRecordCreationEvents(): Flow<EnrolmentRecordCreationEvent> = flow {
        try {
            // First collect all case IDs in a list
            Simber.d("Start listing caseIds", tag = "CommCareSync")
            val caseIds = mutableListOf<String>()
            context.contentResolver
                .query(CASE_METADATA_URI, arrayOf(COLUMN_CASE_ID), null, null, null)
                ?.use { cursor ->
                    while (cursor.moveToNext()) {
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CASE_ID))?.let { caseId ->
                            caseIds.add(caseId)
                        }
                    }
                }
            Simber.d("Finished listing caseIds", tag = "CommCareSync")

            // Process case IDs in batches to avoid large pauses
            val batchSize = 20 // Adjust based on performance testing
            caseIds.chunked(batchSize).forEach { batch ->
                batch.forEach { caseId ->
                    loadEnrolmentRecordCreationEvents(caseId).collect { emit(it) }
                }
            }
        } catch (e: Exception) {
            Simber.e("Error while querying CommCare", e)
        }
    }

    private fun loadEnrolmentRecordCreationEvents(caseId: String): Flow<EnrolmentRecordCreationEvent> = flow {
        // Access Case Data Listing for the caseId
        val caseDataUri = CASE_DATA_URI.buildUpon().appendPath(caseId).build()

        context.contentResolver
            .query(caseDataUri, null, null, null, null)
            ?.use { caseDataCursor ->
                var subjectActions = getSubjectActionsValue(caseDataCursor)
                Simber.d(subjectActions)
                val coSyncEnrolmentRecordEvents = parseRecordEvents(subjectActions)

                coSyncEnrolmentRecordEvents
                    ?.events
                    ?.filterIsInstance<EnrolmentRecordCreationEvent>()
                    ?.forEach { emit(it) }
            }
    }

    private fun loadEnrolmentRecordCreationEventsBySubjectActions(): Flow<EnrolmentRecordCreationEvent> = flow {
        // Access Case Data Listing for the caseId
        val caseDataUri = Uri
            .parse("content://$CALLER_PACKAGE_NAME.case/casedb/data/123")
            .buildUpon()
            .appendQueryParameter("mode", "property_filter")
            .appendQueryParameter("property_name", SIMPRINTS_COSYNC_SUBJECT_ACTIONS)
            .build()

        context.contentResolver
            .query(caseDataUri, null, null, null, null)
            ?.use { caseDataCursor ->
                while (caseDataCursor.moveToNext()) {
                    var subjectActions = getSubjectActionsValue(caseDataCursor)
                    Simber.d(subjectActions)
                    val coSyncEnrolmentRecordEvents = parseRecordEvents(subjectActions)

                    coSyncEnrolmentRecordEvents
                        ?.events
                        ?.filterIsInstance<EnrolmentRecordCreationEvent>()
                        ?.forEach { emit(it) }
                }
            }
    }

    private fun getSubjectActionsValue(caseDataCursor: Cursor): String {
//        Simber.d("Start looking for subjectActions", tag = "CommCareSync")
//        while (caseDataCursor.moveToNext()) {
        val key = caseDataCursor.getString(caseDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID))
        if (key == SIMPRINTS_COSYNC_SUBJECT_ACTIONS) {
//            Simber.d("Found subjectActions", tag = "CommCareSync")
            return caseDataCursor.getString(caseDataCursor.getColumnIndexOrThrow(COLUMN_VALUE))
//            }
        }
        Simber.d("No subjectActions found", tag = "CommCareSync")
        return ""
    }

    private fun parseRecordEvents(subjectActions: String) = subjectActions.takeIf(String::isNotEmpty)?.let {
        try {
            jsonHelper.fromJson<CoSyncEnrolmentRecordEvents>(
                json = it,
                module = coSyncSerializationModule,
                type = object : TypeReference<CoSyncEnrolmentRecordEvents>() {},
            )
        } catch (e: Exception) {
            Simber.e("Error while parsing subjectActions", e)
            null
        }
    }

    private val coSyncSerializationModule = SimpleModule().apply {
        addSerializer(
            TokenizableString::class.java,
            TokenizationClassNameSerializer(),
        )
        addDeserializer(
            TokenizableString::class.java,
            TokenizationClassNameDeserializer(),
        )
        addDeserializer(
            EnrolmentRecordCreationEvent::class.java,
            CoSyncEnrolmentRecordCreationEventDeserializer(),
        )
    }

    companion object {
        // TODO(milen): This is a hardcoded package name. We need to find a way to get the package name dynamically
        const val CALLER_PACKAGE_NAME = "org.commcare.dalvik.debug"

        private val CASE_METADATA_URI: Uri = "content://$CALLER_PACKAGE_NAME.case/casedb/case".toUri()
        private val CASE_DATA_URI: Uri = "content://$CALLER_PACKAGE_NAME.case/casedb/data".toUri()

        const val COLUMN_CASE_ID = "case_id"
        const val COLUMN_DATUM_ID = "datum_id"
        const val COLUMN_VALUE = "value"
    }
}
