package com.simprints.infra.eventsync.sync.down

import androidx.work.WorkInfo
import com.simprints.infra.eventsync.status.models.DownSyncState
import com.simprints.infra.eventsync.status.models.EventSyncState.SyncWorkerInfo
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState.Companion.fromWorkInfo
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.DOWNLOADER
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
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
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DownSyncStateProcessor @Inject constructor(
    private val eventSyncCache: EventSyncCache,
    private val syncWorkersInfoProvider: SyncWorkersInfoProvider,
) {
    fun getLastDownSyncState(): Flow<DownSyncState> = observeLastDownSyncId().flatMapLatest { lastSyncId ->
        syncWorkersInfoProvider.getSyncWorkerInfos(lastSyncId).flatMapLatest { syncWorkers ->
            flow {
                val progress = calculateProgress(syncWorkers)
                val total = calculateTotal(syncWorkers)
                val workersInfo = downloaderStates(syncWorkers)
                val lastSyncTime = eventSyncCache.readLastDownSyncTime()

                emit(
                    DownSyncState(
                        syncId = lastSyncId,
                        progress = progress,
                        total = total,
                        workersInfo = workersInfo,
                        lastSyncTime = lastSyncTime,
                    ),
                )
            }
        }
    }

    private fun observeLastDownSyncId(): Flow<String> = syncWorkersInfoProvider
        .getDownSyncStartReporters()
        .flatMapLatest { startReporters ->
            Simber.d("Received update from DownSync Scheduler", tag = SYNC)
            val completedReporters = startReporters.filter { it.state == WorkInfo.State.SUCCEEDED }
            val mostRecent = completedReporters.sortByScheduledTime().lastOrNull()
            flow {
                val lastSyncId = mostRecent?.outputData?.getString(SYNC_ID_STARTED).orEmpty()
                emit(lastSyncId)
            }
        }

    private suspend fun calculateProgress(workInfos: List<WorkInfo>): Int? {
        if (eventSyncCache.shouldIgnoreMax()) return null
        return workInfos.filterByTags(tagForType(DOWNLOADER))
            .sumOf { it.extractDownSyncProgress(eventSyncCache) }
    }

    private suspend fun calculateTotal(workInfos: List<WorkInfo>): Int? {
        if (eventSyncCache.shouldIgnoreMax()) return null
        return workInfos.filterByTags(tagForType(DOWNLOADER))
            .sumOf { it.extractDownSyncMaxCount(eventSyncCache) }
    }

    private fun downloaderStates(workInfos: List<WorkInfo>): List<SyncWorkerInfo> =
        workInfos.filterByTags(tagForType(DOWNLOADER)).map {
            SyncWorkerInfo(DOWNLOADER, it.toDownSyncWorkerState())
        }

    private fun WorkInfo.toDownSyncWorkerState(): EventSyncWorkerState = fromWorkInfo(
        state = state,
        failedBecauseReloginRequired = didFailBecauseReloginRequired(),
        failedBecauseCloudIntegration = didFailBecauseCloudIntegration(),
        failedBecauseBackendMaintenance = didFailBecauseBackendMaintenance(),
        failedBecauseTooManyRequest = didFailBecauseTooManyRequests(),
        failedBecauseCommCarePermissionMissing = didFailBecauseCommCarePermissionMissing(),
        estimatedOutage = getEstimatedOutageTime(),
    )
}
