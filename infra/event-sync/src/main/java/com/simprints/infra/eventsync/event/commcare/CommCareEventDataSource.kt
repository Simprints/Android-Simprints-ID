package com.simprints.infra.eventsync.event.commcare

import android.content.Context
import android.database.Cursor
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import com.simprints.infra.config.store.LastCallingPackageStore
import com.simprints.infra.events.event.cosync.CoSyncEnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.EnrolmentRecordDeletionEvent
import com.simprints.infra.events.event.domain.models.EnrolmentRecordEvent
import com.simprints.infra.eventsync.event.commcare.cache.CommCareSyncCache
import com.simprints.infra.eventsync.event.commcare.cache.SyncedCaseEntity
import com.simprints.infra.eventsync.status.down.domain.CommCareEventSyncResult
import com.simprints.infra.eventsync.status.down.domain.RemoteEventQuery
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.COMMCARE_SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.serialization.SimJson
import com.simprints.libsimprints.Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

internal class CommCareEventDataSource @Inject constructor(
    private val commCareSyncCache: CommCareSyncCache,
    private val lastCallingPackageStore: LastCallingPackageStore,
    @ApplicationContext private val context: Context,
) {
    private val pendingSyncedCases = CopyOnWriteArrayList<SyncedCaseEntity>()

    // Case IDs no longer in CommCare to drop from the cache without deleting their subject.
    // Applied in onEventsProcessed so the cache only changes once the emitted events are processed.
    private val pendingCaseIdsToDrop = CopyOnWriteArrayList<String>()

    @VisibleForTesting
    internal var lastSyncSummary: SyncSummary? = null

    // Pre-created date formatters to avoid repeated instantiation during sync
    private val commCareDateFormats = listOf(
        SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US), // Standard Date.toString() format
        SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US), // Numeric timezone fallback
    )

    fun getEvents(queryEvent: RemoteEventQuery): CommCareEventSyncResult {
        // Clear any leftover state from previous syncs
        pendingSyncedCases.clear()
        pendingCaseIdsToDrop.clear()

        return if (queryEvent.externalIds.isNullOrEmpty()) {
            CommCareEventSyncResult(
                totalCount = count(),
                eventFlow = syncAllUpdatedCases(),
            )
        } else {
            CommCareEventSyncResult(
                totalCount = queryEvent.externalIds.size,
                eventFlow = syncSpecificCases(queryEvent.externalIds),
            )
        }
    }

    private fun syncSpecificCases(caseIds: List<String>) = flow {
        caseIds.forEach { caseId ->
            Simber.i("Processing caseId: $caseId", tag = COMMCARE_SYNC)
            var commCareLastModifiedTime = 0L
            val caseMetadataUri = getCaseMetadataUri().buildUpon().appendPath(caseId).build()
            context.contentResolver
                .query(caseMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), null, null, null)
                ?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val commCareLastModifiedString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_MODIFIED))
                        commCareLastModifiedTime = parseCommCareDateToMillis(commCareLastModifiedString)
                    }
                }

            // We don't know the simprintsId until we parse its subjectActions so it will be filled later
            loadEnrolmentRecordCreationEvents(SyncedCaseEntity(caseId, "", commCareLastModifiedTime))
                .collect {
                    emit(it)
                }
        }
    }

    private fun count(): Int {
        var count = 0
        context.contentResolver
            .query(getCaseMetadataUri(), null, null, null, null)
            ?.use { caseMetadataCursor -> count = caseMetadataCursor.count }
        return count
    }

    private fun syncAllUpdatedCases(): Flow<EnrolmentRecordEvent> = flow {
        try {
            Simber.i("Start listing caseIds for CommCare sync", tag = COMMCARE_SYNC)

            val casesToParse = mutableListOf<SyncedCaseEntity>()
            val caseIdsPresentInCommCare = mutableSetOf<String>()
            // Subjects referenced by at least one case still present in CommCare. The same subject can be
            // referenced by several cases (e.g. a closed form case and the client case), so a subject must
            // be kept as long as any of them remains, even if CommCare purged the others from the device.
            val subjectIdsReferencedInCommCare = mutableSetOf<String>()
            val summary = SyncSummary()
            // Fetch all previously synced cases with their details (including lastSyncedTimestamp)
            val previouslySyncedCasesMap = commCareSyncCache
                .getAllSyncedCases()
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
                                    tag = COMMCARE_SYNC,
                                )
                                subjectIdsReferencedInCommCare.add(cachedCase.simprintsId)
                                summary.skippedCount++
                                continue // Skip cases not modified since last sync
                            }
                        }

                        // We don't know the simprintsId until we parse its subjectActions so it will be filled later
                        casesToParse.add(SyncedCaseEntity(caseId, "", commCareLastModifiedTime))
                    }
                }
            summary.listedCount = caseIdsPresentInCommCare.size
            summary.parsedCount = casesToParse.size

            // Process case IDs in batches to avoid large pauses
            val batchSize = BATCH_SIZE // Adjust based on performance testing
            casesToParse.chunked(batchSize).forEach { batch ->
                batch.forEach { case ->
                    var hasSubject = false
                    loadEnrolmentRecordCreationEvents(case).collect { event ->
                        hasSubject = true
                        subjectIdsReferencedInCommCare.add(event.payload.subjectId)
                        summary.created.add(case.caseId to event.payload.subjectId)
                        emit(event)
                    }
                    if (!hasSubject) summary.parsedWithoutSubjectCount++
                }
            }

            // If no cases were found in CommCare, it's most likely that CommCare is logged out.
            if (caseIdsPresentInCommCare.isNotEmpty()) {
                val casesNoLongerInCommCare = previouslySyncedCasesMap.values.filterNot { (it.caseId in caseIdsPresentInCommCare) }
                // Cases without a subject, or whose subject is still referenced by another case, are only dropped from the cache
                val (casesToDrop, casesToRemove) = casesNoLongerInCommCare.partition {
                    it.simprintsId.isEmpty() || it.simprintsId in subjectIdsReferencedInCommCare
                }
                pendingCaseIdsToDrop.addAll(casesToDrop.map { it.caseId })
                summary.dropped = casesToDrop.map { it.caseId to it.simprintsId }
                summary.deleted = casesToRemove.map { it.caseId to it.simprintsId }
                casesToRemove.forEach { case ->
                    generateEnrolmentRecordDeletionEvent(case).collect {
                        emit(it)
                    }
                }
            }
            lastSyncSummary = summary
            Simber.i(summary.toLogMessage(), tag = COMMCARE_SYNC)
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
        emit(
            EnrolmentRecordDeletionEvent(
                subjectId = case.simprintsId,
                projectId = "", // Only subjectId is required for deletion events
                moduleId = "",
                attendantId = "",
            ),
        )
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

                if (coSyncEnrolmentRecordEvents == null) {
                    Simber.d("No valid enrolment records found for caseId ${case.caseId}.", tag = COMMCARE_SYNC)
                    // Add the case to the cache with an empty simprintsId so that we don't try to sync it again until updated
                    commCareSyncCache.addSyncedCase(case)
                    Simber.d("Added case ${case.caseId} with empty simprintsId to CommCareSyncCache", tag = COMMCARE_SYNC)
                    return@flow
                }

                coSyncEnrolmentRecordEvents
                    .toDomainEvents()
                    .filterIsInstance<EnrolmentRecordCreationEvent>()
                    .forEach { event ->
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
            SimJson.decodeFromString<CoSyncEnrolmentRecordEvents>(it)
        } catch (e: Exception) {
            Simber.e("Error while parsing subjectActions", e)
            null
        }
    }

    private fun getCaseMetadataUri() = "content://${lastCallingPackageStore.lastCallingPackageName}.case/casedb/case".toUri()

    private fun getCaseDataUri() = "content://${lastCallingPackageStore.lastCallingPackageName}.case/casedb/data".toUri()

    private fun parseCommCareDateToMillis(dateString: String): Long {
        for (format in commCareDateFormats) {
            try {
                return format.parse(dateString)?.time ?: 0L
            } catch (e: Exception) {
                Simber.e("Error parsing date: $dateString", e, tag = COMMCARE_SYNC)
                continue
            }
        }

        Simber.w(
            message = "All date parsing attempts failed for: $dateString",
            t = IllegalArgumentException("Could not parse date string"),
            tag = COMMCARE_SYNC,
        )
        return 0L
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
                is EnrolmentRecordCreationEvent -> {
                    creationSubjectIds.add(event.payload.subjectId)
                }

                is EnrolmentRecordDeletionEvent -> {
                    deletionSubjectIds.add(event.payload.subjectId)
                }

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

        // Events emitted before these cases were queued have been processed by now, so they can be dropped safely
        val caseIdsToDrop = pendingCaseIdsToDrop.toList()
        caseIdsToDrop.forEach { caseId ->
            commCareSyncCache.removeSyncedCase(caseId)
            Simber.d("Dropped case $caseId from CommCareSyncCache without deleting its subject", tag = COMMCARE_SYNC)
        }
        pendingCaseIdsToDrop.removeAll(caseIdsToDrop)
    }

    /**
     * Collects what a CommCare sync did so it can be logged as one line that makes it to release logs.
     * Deleted and dropped cases are always listed in full as they are rare and key to debugging missing subjects.
     */
    internal class SyncSummary {
        var listedCount = 0
        var skippedCount = 0
        var parsedCount = 0
        var parsedWithoutSubjectCount = 0
        val created = mutableListOf<Pair<String, String>>()
        var deleted: List<Pair<String, String>>? = null
        var dropped: List<Pair<String, String>>? = null

        fun toLogMessage(): String = buildString {
            append("CommCare sync: listed $listedCount, skipped $skippedCount (unchanged), ")
            append("parsed $parsedCount (no subject $parsedWithoutSubjectCount), ")
            append("created ${created.size} ${created.format(MAX_LOGGED_CREATED_CASES)}, ")
            val deleted = deleted
            val dropped = dropped
            if (deleted == null || dropped == null) {
                append("deletions skipped as CommCare returned no cases")
            } else {
                append("deleted ${deleted.size} ${deleted.format()}, ")
                append("dropped ${dropped.size} ${dropped.format()}")
            }
        }

        private fun List<Pair<String, String>>.format(limit: Int = size): String {
            val shown = take(limit).joinToString { (caseId, subjectId) -> "$caseId→${subjectId.ifEmpty { "none" }}" }
            val more = if (size > limit) ", … +${size - limit} more" else ""
            return "[$shown$more]"
        }
    }

    companion object {
        internal const val MAX_LOGGED_CREATED_CASES = 20
        internal const val COLUMN_CASE_ID = "case_id"
        internal const val COLUMN_LAST_MODIFIED = "last_modified"
        internal const val COLUMN_DATUM_ID = "datum_id"
        internal const val COLUMN_VALUE = "value"
        internal const val BATCH_SIZE = 20
    }
}
