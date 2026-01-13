package com.simprints.infra.eventsync.sync

import androidx.work.WorkInfo
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncState.SyncWorkerInfo
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Companion.fromWorkInfo
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.DOWNLOADER
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.END_SYNC_REPORTER
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.START_SYNC_REPORTER
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.UPLOADER
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.SyncWorkersInfoProvider
import com.simprints.infra.eventsync.sync.common.didFailBecauseBackendMaintenance
import com.simprints.infra.eventsync.sync.common.didFailBecauseCloudIntegration
import com.simprints.infra.eventsync.sync.common.didFailBecauseCommCarePermissionMissing
import com.simprints.infra.eventsync.sync.common.didFailBecauseReloginRequired
import com.simprints.infra.eventsync.sync.common.didFailBecauseTooManyRequests
import com.simprints.infra.eventsync.sync.common.filterByTags
import com.simprints.infra.eventsync.sync.common.getEstimatedOutageTime
import com.simprints.infra.eventsync.sync.common.sortByScheduledTime
import com.simprints.infra.eventsync.sync.down.workers.extractDownSyncMaxCount
import com.simprints.infra.eventsync.sync.down.workers.extractDownSyncProgress
import com.simprints.infra.eventsync.sync.master.EventStartSyncReporterWorker.Companion.SYNC_ID_STARTED
import com.simprints.infra.eventsync.sync.up.workers.extractUpSyncMaxCount
import com.simprints.infra.eventsync.sync.up.workers.extractUpSyncProgress
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class EventSyncStateProcessor @Inject constructor(
    private val eventSyncCache: EventSyncCache,
    private val syncWorkersInfoProvider: SyncWorkersInfoProvider,
) {
    fun getLastSyncState(): Flow<EventSyncState> = observerForLastSyncId().flatMapLatest { lastSyncId ->
        observerForLastSyncIdWorkers(lastSyncId).flatMapLatest { syncWorkers ->
            flow {
                val progress = calculateProgressForSync(syncWorkers)
                val total = calculateTotalForSync(syncWorkers)

                val upSyncStates = upSyncUploadersStates(syncWorkers)
                val downSyncStates = downSyncDownloadersStates(syncWorkers)

                val syncReporterStates = syncStartReporterStates(syncWorkers) + syncEndReporterStates(syncWorkers)

                val syncState = EventSyncState(
                    lastSyncId,
                    progress,
                    total,
                    upSyncStates,
                    downSyncStates,
                    syncReporterStates,
                )

                Simber.d("Emitting for sync state: $syncState", tag = SYNC)
                emit(syncState)
            }
        }
    }

    private fun observerForLastSyncId(): Flow<String> = syncWorkersInfoProvider
        .getStartSyncReporters()
        .flatMapLatest { startSyncReporters ->
            Simber.d("Received updated from Master Scheduler", tag = SYNC)

            val completedSyncMaster = completedWorkers(startSyncReporters)
            val mostRecentSyncMaster = completedSyncMaster.sortByScheduledTime().lastOrNull()

            flow {
                if (mostRecentSyncMaster != null) {
                    val lastSyncId = mostRecentSyncMaster.outputData.getString(SYNC_ID_STARTED)
                    if (!lastSyncId.isNullOrBlank()) {
                        Simber.d("Received sync id: $lastSyncId", tag = SYNC)
                        emit(lastSyncId)
                    }
                }
            }
        }

    private fun observerForLastSyncIdWorkers(lastSyncId: String) = syncWorkersInfoProvider.getSyncWorkerInfos(lastSyncId)

    private fun completedWorkers(workInfos: List<WorkInfo>) = workInfos.filter { it.state == WorkInfo.State.SUCCEEDED }

    private suspend fun calculateProgressForSync(workInfos: List<WorkInfo>): Int? {
        if (eventSyncCache.shouldIgnoreMax()) {
            return null
        }

        val totalDown = calculateProgressForDownSync(workInfos)
        val totalUp = calculateProgressForUpSync(workInfos)
        return totalUp + totalDown
    }

    private suspend fun calculateTotalForSync(workInfos: List<WorkInfo>): Int? {
        if (eventSyncCache.shouldIgnoreMax()) {
            return null
        }

        val totalDown = calculateTotalForDownSync(workInfos)
        val totalUp = calculateTotalForUpSync(workInfos)
        return totalUp + totalDown
    }

    private suspend fun calculateTotalForDownSync(workInfos: List<WorkInfo>): Int = workInfos
        .filterByTags(tagForType(DOWNLOADER))
        .sumOf { worker -> worker.extractDownSyncMaxCount(eventSyncCache) }

    private suspend fun calculateTotalForUpSync(workInfos: List<WorkInfo>): Int = workInfos
        .filterByTags(tagForType(UPLOADER))
        .sumOf { worker -> worker.extractUpSyncMaxCount(eventSyncCache) }

    private fun upSyncUploadersStates(workInfos: List<WorkInfo>): List<SyncWorkerInfo> = workInfos.filterByTags(tagForType(UPLOADER)).map {
        SyncWorkerInfo(UPLOADER, it.toEventSyncWorkerState())
    }

    private fun downSyncDownloadersStates(workInfos: List<WorkInfo>): List<SyncWorkerInfo> =
        workInfos.filterByTags(tagForType(DOWNLOADER)).map {
            SyncWorkerInfo(DOWNLOADER, it.toEventSyncWorkerState())
        }

    private fun syncStartReporterStates(workInfos: List<WorkInfo>): List<SyncWorkerInfo> =
        workInfos.filterByTags(tagForType(START_SYNC_REPORTER)).map {
            SyncWorkerInfo(START_SYNC_REPORTER, it.toEventSyncWorkerState())
        }

    private fun syncEndReporterStates(workInfos: List<WorkInfo>): List<SyncWorkerInfo> =
        workInfos.filterByTags(tagForType(END_SYNC_REPORTER)).map {
            SyncWorkerInfo(END_SYNC_REPORTER, it.toEventSyncWorkerState())
        }

    private fun WorkInfo.toEventSyncWorkerState(): EventSyncWorkerState = fromWorkInfo(
        state = state,
        failedBecauseReloginRequired = didFailBecauseReloginRequired(),
        failedBecauseCloudIntegration = didFailBecauseCloudIntegration(),
        failedBecauseBackendMaintenance = didFailBecauseBackendMaintenance(),
        failedBecauseTooManyRequest = didFailBecauseTooManyRequests(),
        failedBecauseCommCarePermissionMissing = didFailBecauseCommCarePermissionMissing(),
        estimatedOutage = getEstimatedOutageTime(),
    )

    private suspend fun calculateProgressForDownSync(workInfos: List<WorkInfo>): Int {
        val downWorkers = workInfos.filterByTags(tagForType(DOWNLOADER))
        val progresses = downWorkers.map { worker ->
            worker.extractDownSyncProgress(eventSyncCache)
        }

        return progresses.sum()
    }

    private suspend fun calculateProgressForUpSync(workInfos: List<WorkInfo>): Int {
        val upWorkers = workInfos.filterByTags(tagForType(UPLOADER))
        val progresses = upWorkers.map { worker ->
            worker.extractUpSyncProgress(eventSyncCache)
        }

        return progresses.sum()
    }
}
