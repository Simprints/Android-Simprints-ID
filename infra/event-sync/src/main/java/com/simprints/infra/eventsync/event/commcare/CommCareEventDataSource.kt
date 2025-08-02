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
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.eventsync.event.commcare.cache.CommCareSyncCache
import com.simprints.infra.eventsync.event.commcare.cache.SyncedCaseEntity
import com.simprints.infra.eventsync.status.down.domain.CommCareEventSyncResult
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.COMMCARE_SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.resources.R as IDR
import com.simprints.libsimprints.Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

internal class CommCareEventDataSource @Inject constructor(
    private val jsonHelper: JsonHelper,
    private val commCareSyncCache: CommCareSyncCache,
    @ApplicationContext private val context: Context,
) {

    private val pendingSyncedCases = CopyOnWriteArrayList<SyncedCaseEntity>()
    fun getEvents(): CommCareEventSyncResult {
        val totalCount = count()
        val eventFlow = loadDataFromCommCare()

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

    private fun loadDataFromCommCare(): Flow<EnrolmentRecordEvent> = flow {
        try {
            Simber.i("Start listing caseIds for CommCare sync", tag = COMMCARE_SYNC)

            val casesToParse = mutableListOf<SyncedCaseEntity>()
            val caseIdsPresentInCommCare = mutableListOf<String>()
            // Fetch all previously synced cases with their details (including lastSyncedTimestamp)
            val previouslySyncedCasesMap = commCareSyncCache.getAllSyncedCases()
                .associateBy { it.caseId }

            context.contentResolver
                .query(getCaseMetadataUri(), arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), null, null, null)
                ?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val caseId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CASE_ID))
                        if (caseId.isNullOrEmpty()) {
                            continue // Skip empty case IDs
                        }
                        caseIdsPresentInCommCare.add(caseId)

                        val commCareLastModifiedString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_MODIFIED))
                        val commCareLastModifiedTime = parseCommCareDateToMillis(commCareLastModifiedString)

                        val cachedCase = previouslySyncedCasesMap[caseId]
                        if (cachedCase != null) {
                            // Case was synced before, check its specific lastSyncedTimestamp
                            if (commCareLastModifiedTime > 0L && commCareLastModifiedTime <= cachedCase.lastSyncedTimestamp) {
                                Simber.d(
                                    "Skipping caseId $caseId: CommCare lastModified ($commCareLastModifiedTime) is not newer than lastSyncedTimestamp (${cachedCase.lastSyncedTimestamp})",
                                    tag = COMMCARE_SYNC
                                )
                                continue // Skip cases not modified since last sync
                            }
                        }

                        casesToParse.add(SyncedCaseEntity(caseId, "", commCareLastModifiedTime))
                    }
                }
            Simber.i("Finished listing caseIds. ${casesToParse.size} cases to parse.", tag = COMMCARE_SYNC)

            // Process case IDs in batches to avoid large pauses
            val batchSize = BATCH_SIZE // Adjust based on performance testing
            casesToParse.chunked(batchSize).forEach { batch ->
                batch.forEach { case ->
                    loadEnrolmentRecordCreationEvents(case).collect { emit(it) }
                }
            }

            // If no cases were found in CommCare, it's most likely that CommCare is logged out.
            if (caseIdsPresentInCommCare.isNotEmpty()) {
                val casesToRemove = previouslySyncedCasesMap.values.filterNot { (it.caseId in caseIdsPresentInCommCare) }
                Simber.i("Generating deletion events for ${casesToRemove.size} cases no longer in CommCare.", tag = COMMCARE_SYNC)
                casesToRemove.forEach { case ->
                    generateEnrolmentRecordDeletionEvent(case).collect { emit(it) }
                }
            }
        } catch (e: Exception) {
            Simber.e("Error during CommCare data loading", e, tag = COMMCARE_SYNC)
            throw e // Rethrow to let the sync worker handle the failure
        }
    }

    /* Generates deletion events for enrolment records that were previously synced but are no longer present in CommCare.
     * This is called when a case is not found in the latest sync.
     */
    private fun generateEnrolmentRecordDeletionEvent(case: SyncedCaseEntity): Flow<EnrolmentRecordDeletionEvent> = flow {
        Simber.d("Generating deletion event for caseId ${case.caseId} with simprintsId ${case.simprintsId}", tag = COMMCARE_SYNC)
        pendingSyncedCases.add(case)
        emit(EnrolmentRecordDeletionEvent(
            subjectId = case.simprintsId,
            projectId = "",
            moduleId = "",
            attendantId = "",
        ))
    }

    private fun loadEnrolmentRecordCreationEvents(case: SyncedCaseEntity): Flow<EnrolmentRecordCreationEvent> = flow {
        // Access Case Data Listing for the caseId
        val caseDataUri = getCaseDataUri().buildUpon().appendPath(case.caseId).build()

        val cursor = context.contentResolver
            .query(caseDataUri, null, null, null, null)
        if (cursor != null) {
            cursor.use { caseDataCursor ->
                val subjectActions = getSubjectActionsValue(caseDataCursor)
                Simber.d(subjectActions)
                val coSyncEnrolmentRecordEvents = parseRecordEvents(subjectActions)

                coSyncEnrolmentRecordEvents
                    ?.events
                    ?.filterIsInstance<EnrolmentRecordCreationEvent>()
                    ?.forEach { event ->
                        pendingSyncedCases.add(case.copy(simprintsId = event.payload.subjectId))
                        emit(event)
                    }
            }
        } else {
            // If listing returned the caseId but the cursor is null, most likely CommCare
            // logged out in the middle of sync. Throw an exception to retry the worker
            // instead of thinking sync is complete (and possibly deleting unsynced subjects).
            throw IllegalStateException("Cursor for caseId ${case.caseId} is null")
        }
    }

    private fun getSubjectActionsValue(caseDataCursor: Cursor): String {
        Simber.d("Start looking for subjectActions", tag = COMMCARE_SYNC)
        while (caseDataCursor.moveToNext()) {
            val key = caseDataCursor.getString(caseDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID))
            if (key == SIMPRINTS_COSYNC_SUBJECT_ACTIONS) {
                Simber.d("Found subjectActions", tag = COMMCARE_SYNC)
                return caseDataCursor.getString(caseDataCursor.getColumnIndexOrThrow(COLUMN_VALUE))
            }
        }
        Simber.d("No subjectActions found", tag = COMMCARE_SYNC)
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

    private fun parseCommCareDateToMillis(dateString: String): Long {
        val format = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault())
        return try {
            format.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            Simber.e("Error parsing date: $dateString", e, tag = COMMCARE_SYNC)
            0L
        }
    }

    /**
     * This function is called after all events have been processed.
     * It updates the CommCareSyncCache with the latest case IDs and their corresponding Simprints IDs.
     */
    suspend fun onEventsProcessed(events: List<EnrolmentRecordEvent>) {
        val creationSubjectIds = mutableSetOf<String>()
        val deletionSubjectIds = mutableSetOf<String>()

        events.forEach { event ->
            when (event) {
                is EnrolmentRecordCreationEvent -> creationSubjectIds.add(event.payload.subjectId)
                is EnrolmentRecordDeletionEvent -> deletionSubjectIds.add(event.payload.subjectId)
                else -> { /* Ignore other event types */ }
            }
        }

        val pendingCasesToRemove = mutableListOf<SyncedCaseEntity>()

        pendingSyncedCases.forEach { case ->
            when (case.simprintsId) {
                in creationSubjectIds -> {
                    commCareSyncCache.addSyncedCase(case)
                    Simber.d("Added case ${case.caseId} with simprintsId ${case.simprintsId} to CommCareSyncCache", tag = COMMCARE_SYNC)
                    pendingCasesToRemove.add(case)
                }
                in deletionSubjectIds -> {
                    commCareSyncCache.removeSyncedCase(case.caseId)
                    Simber.d("Removed case ${case.caseId} with simprintsId ${case.simprintsId} from CommCareSyncCache", tag = COMMCARE_SYNC)
                    pendingCasesToRemove.add(case)
                }
            }
        }

        // Remove processed cases from pendingSyncedCases
        pendingSyncedCases.removeAll(pendingCasesToRemove)
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
        internal const val COLUMN_CASE_ID = "case_id"
        internal const val COLUMN_LAST_MODIFIED = "last_modified"
        internal const val COLUMN_DATUM_ID = "datum_id"
        internal const val COLUMN_VALUE = "value"
        internal const val BATCH_SIZE = 20
    }
}
