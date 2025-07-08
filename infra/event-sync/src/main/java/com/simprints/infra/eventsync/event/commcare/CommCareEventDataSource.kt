package com.simprints.infra.eventsync.event.commcare

import android.content.Context
import android.database.Cursor
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
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
import com.simprints.infra.resources.R as IDR
import com.simprints.libsimprints.Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal class CommCareEventDataSource @Inject constructor(
    private val jsonHelper: JsonHelper,
    @ApplicationContext private val context: Context,
) {
    fun getEvents(): CommCareEventSyncResult {
        val totalCount = count()
        val eventFlow = loadEnrolmentRecordCreationEvents()

        return CommCareEventSyncResult(
            totalCount = totalCount,
            eventFlow = eventFlow,
        )
    }

    private fun count(): Int {
        var count = 0
        context.contentResolver
            .query(getCaseMetadataUri(), null, null, null, null)
            ?.use { caseMetadataCursor -> count = caseMetadataCursor.count }
        return count
    }

    private fun loadEnrolmentRecordCreationEvents(): Flow<EnrolmentRecordCreationEvent> = flow {
        try {
            // First collect all case IDs in a list
            Simber.d("Start listing caseIds", tag = "CommCareSync")
            val caseIds = mutableListOf<String>()
            context.contentResolver
                .query(getCaseMetadataUri(), arrayOf(COLUMN_CASE_ID), null, null, null)
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
            throw e
        }
    }

    private fun loadEnrolmentRecordCreationEvents(caseId: String): Flow<EnrolmentRecordCreationEvent> = flow {
        // Access Case Data Listing for the caseId
        val caseDataUri = getCaseDataUri().buildUpon().appendPath(caseId).build()

        val cursor = context.contentResolver
            .query(caseDataUri, null, null, null, null)
        Simber.d("Cursor for caseId $caseId: $cursor", tag = "CommCareSync")
        if (cursor != null) {
            cursor.use { caseDataCursor ->
                val subjectActions = getSubjectActionsValue(caseDataCursor)
                Simber.d(subjectActions)
                val coSyncEnrolmentRecordEvents = parseRecordEvents(subjectActions)

                coSyncEnrolmentRecordEvents
                    ?.events
                    ?.filterIsInstance<EnrolmentRecordCreationEvent>()
                    ?.forEach { emit(it) }
            }
        } else {
            // If listing returned the caseId but the cursor is null, most likely CommCare
            // logged out in the middle of sync. Throw an exception to retry the worker
            // instead of thinking sync is complete (and possibly deleting unsynced subjects).
            throw IllegalStateException("Cursor for caseId $caseId is null")
        }
    }

    private fun getSubjectActionsValue(caseDataCursor: Cursor): String {
        Simber.d("Start looking for subjectActions", tag = "CommCareSync_extra")
        while (caseDataCursor.moveToNext()) {
            val key = caseDataCursor.getString(caseDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID))
            if (key == SIMPRINTS_COSYNC_SUBJECT_ACTIONS) {
                Simber.d("Found subjectActions", tag = "CommCareSync_extra")
                return caseDataCursor.getString(caseDataCursor.getColumnIndexOrThrow(COLUMN_VALUE))
            }
        }
        Simber.d("No subjectActions found", tag = "CommCareSync_extra")
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

    private fun getPackageName() = PreferenceManager.getDefaultSharedPreferences(context)
        .getString(
            context.getString(IDR.string.preference_last_calling_package_name_key),
            context.getString(IDR.string.default_commcare_package_name)
        ) ?: context.getString(IDR.string.default_commcare_package_name)

    private fun getCaseMetadataUri() = "content://${getPackageName()}.case/casedb/case".toUri()

    private fun getCaseDataUri() = "content://${getPackageName()}.case/casedb/data".toUri()

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
        const val COLUMN_CASE_ID = "case_id"
        const val COLUMN_DATUM_ID = "datum_id"
        const val COLUMN_VALUE = "value"
    }
}
