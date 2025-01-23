package com.simprints.infra.eventsync.sync

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
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
import com.simprints.infra.eventsync.sync.common.SyncWorkersLiveDataProvider
import com.simprints.infra.eventsync.sync.common.didFailBecauseBackendMaintenance
import com.simprints.infra.eventsync.sync.common.didFailBecauseCloudIntegration
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
import javax.inject.Inject

internal class EventSyncStateProcessor @Inject constructor(
    private val eventSyncCache: EventSyncCache,
    private val syncWorkersLiveDataProvider: SyncWorkersLiveDataProvider,
) {
    fun getLastSyncState(): LiveData<EventSyncState> = observerForLastSyncId().switchMap { lastSyncId ->
        observerForLastSyncIdWorkers(lastSyncId).switchMap { syncWorkers ->
            liveData {
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

                emit(syncState)
                Simber.d("Emitting for sync state: $syncState", tag = SYNC)
            }
        }
    }

    private fun observerForLastSyncId(): LiveData<String> = syncWorkersLiveDataProvider
        .getStartSyncReportersLiveData()
        .switchMap { startSyncReporters ->
            Simber.d("Received updated from Master Scheduler", tag = SYNC)

            val completedSyncMaster = completedWorkers(startSyncReporters)
            val mostRecentSyncMaster = completedSyncMaster.sortByScheduledTime().lastOrNull()

            MutableLiveData<String>().apply {
                if (mostRecentSyncMaster != null) {
                    val lastSyncId = mostRecentSyncMaster.outputData.getString(SYNC_ID_STARTED)
                    if (!lastSyncId.isNullOrBlank()) {
                        Simber.d("Received sync id: $lastSyncId", tag = SYNC)
                        this.postValue(lastSyncId)
                    }
                }
            }
        }

    private fun observerForLastSyncIdWorkers(lastSyncId: String) = syncWorkersLiveDataProvider.getSyncWorkersLiveData(lastSyncId)

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
        state,
        didFailBecauseReloginRequired(),
        didFailBecauseCloudIntegration(),
        didFailBecauseBackendMaintenance(),
        didFailBecauseTooManyRequests(),
        getEstimatedOutageTime(),
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
