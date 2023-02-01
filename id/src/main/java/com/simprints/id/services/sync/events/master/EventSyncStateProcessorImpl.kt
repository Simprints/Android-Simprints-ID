package com.simprints.id.services.sync.events.master

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.work.WorkInfo
import com.simprints.eventsystem.events_sync.models.EventSyncState
import com.simprints.eventsystem.events_sync.models.EventSyncState.SyncWorkerInfo
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerState
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerState.Companion.fromWorkInfo
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerType.*
import com.simprints.eventsystem.events_sync.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.common.filterByTags
import com.simprints.id.services.sync.events.common.sortByScheduledTime
import com.simprints.id.services.sync.events.down.workers.extractDownSyncProgress
import com.simprints.id.services.sync.events.down.workers.getDownCountsFromOutput
import com.simprints.id.services.sync.events.master.internal.*
import com.simprints.id.services.sync.events.master.workers.EventStartSyncReporterWorker.Companion.SYNC_ID_STARTED
import com.simprints.id.services.sync.events.up.workers.extractUpSyncProgress
import com.simprints.id.services.sync.events.up.workers.getUpCountsFromOutput
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class EventSyncStateProcessorImpl @Inject constructor(
    private val eventSyncCache: EventSyncCache,
    private val syncWorkersLiveDataProvider: SyncWorkersLiveDataProvider
) : EventSyncStateProcessor {

    override fun getLastSyncState(): LiveData<EventSyncState> =
        observerForLastSyncId().switchMap { lastSyncId ->
            observerForLastSyncIdWorkers(lastSyncId).switchMap { syncWorkers ->
                liveData {
                    val progress = calculateProgressForDownSync(syncWorkers) + calculateProgressForUpSync(syncWorkers)
                    val total = calculateTotalForSync(syncWorkers)

                    val upSyncStates = upSyncUploadersStates(syncWorkers) + upSyncCountersStates(syncWorkers)
                    val downSyncStates = downSyncDownloadersStates(syncWorkers) + downSyncCountersStates(syncWorkers)

                    val syncState = EventSyncState(
                        lastSyncId,
                        progress,
                        total,
                        upSyncStates,
                        downSyncStates
                    )

                    emit(syncState)
                    Simber.tag(SYNC_LOG_TAG).d("[PROCESSOR] Emitting for UI $syncState")
                }
            }
        }


    private fun observerForLastSyncId(): LiveData<String> {
        return syncWorkersLiveDataProvider.getStartSyncReportersLiveData()
            .switchMap { startSyncReporters ->
                Simber.tag(SYNC_LOG_TAG).d("[PROCESSOR] Received updated from Master Scheduler")

                val completedSyncMaster = completedWorkers(startSyncReporters)
                val mostRecentSyncMaster = completedSyncMaster.sortByScheduledTime().lastOrNull()

                MutableLiveData<String>().apply {
                    if (mostRecentSyncMaster != null) {
                        val lastSyncId = mostRecentSyncMaster.outputData.getString(SYNC_ID_STARTED)
                        if (!lastSyncId.isNullOrBlank()) {
                            Simber.tag(SYNC_LOG_TAG).d("[PROCESSOR] Received sync id: $lastSyncId")
                            this.postValue(lastSyncId)
                        }
                    }
                }
            }
    }

    private fun observerForLastSyncIdWorkers(lastSyncId: String) =
        syncWorkersLiveDataProvider.getSyncWorkersLiveData(lastSyncId)

    private fun completedWorkers(workInfos: List<WorkInfo>) =
        workInfos.filter { it.state == WorkInfo.State.SUCCEEDED }

    private fun calculateTotalForSync(workInfos: List<WorkInfo>): Int? {
        val totalDown = calculateTotalForDownSync(workInfos)
        val totalUp = calculateTotalForUpSync(workInfos)
        return if (totalUp != null && totalDown != null) {
            totalUp + totalDown
        } else {
            null
        }
    }

    private fun calculateTotalForDownSync(workInfos: List<WorkInfo>): Int? {
        val countersCompleted = completedWorkers(workInfos.filterByTags(tagForType(DOWN_COUNTER)))
        val counter = countersCompleted.firstOrNull()
        return counter?.getDownCountsFromOutput()?.sumOf { it.count }
    }

    private fun calculateTotalForUpSync(workInfos: List<WorkInfo>): Int? {
        val countersCompleted = completedWorkers(workInfos.filterByTags(tagForType(UP_COUNTER)))
        val counter = countersCompleted.firstOrNull()
        return counter?.getUpCountsFromOutput()
    }

    private fun upSyncUploadersStates(workInfos: List<WorkInfo>): List<SyncWorkerInfo> =
        workInfos.filterByTags(tagForType(UPLOADER)).map {
            SyncWorkerInfo(UPLOADER, it.toEventSyncWorkerState())
        }

    private fun downSyncDownloadersStates(workInfos: List<WorkInfo>): List<SyncWorkerInfo> =
        workInfos.filterByTags(tagForType(DOWNLOADER)).map {
            SyncWorkerInfo(DOWNLOADER, it.toEventSyncWorkerState())
        }

    private fun downSyncCountersStates(workInfos: List<WorkInfo>): List<SyncWorkerInfo> =
        workInfos.filterByTags(tagForType(DOWN_COUNTER)).map {
            SyncWorkerInfo(DOWN_COUNTER, it.toEventSyncWorkerState())
        }

    private fun upSyncCountersStates(workInfos: List<WorkInfo>): List<SyncWorkerInfo> =
        workInfos.filterByTags(tagForType(UP_COUNTER)).map {
            SyncWorkerInfo(UP_COUNTER, it.toEventSyncWorkerState())
        }

    private fun WorkInfo.toEventSyncWorkerState(): EventSyncWorkerState =
        fromWorkInfo(
            state,
            didFailBecauseCloudIntegration(),
            didFailBecauseBackendMaintenance(),
            didFailBecauseTooManyRequests(),
            getEstimatedOutageTime()
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
