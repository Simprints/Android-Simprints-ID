package com.simprints.id.services.sync.events.master

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.work.WorkInfo
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.common.filterByTags
import com.simprints.id.services.sync.events.common.sortByScheduledTime
import com.simprints.id.services.sync.events.down.workers.extractDownSyncProgress
import com.simprints.id.services.sync.events.down.workers.getDownCountsFromOutput
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.internal.SyncWorkersLiveDataProvider
import com.simprints.id.services.sync.events.master.internal.SyncWorkersLiveDataProviderImpl
import com.simprints.id.services.sync.events.master.internal.didFailBecauseCloudIntegration
import com.simprints.id.services.sync.events.master.models.SubjectsSyncState
import com.simprints.id.services.sync.events.master.models.SubjectsSyncState.SyncWorkerInfo
import com.simprints.id.services.sync.events.master.models.SubjectsSyncWorkerState.Companion.fromWorkInfo
import com.simprints.id.services.sync.events.master.models.SubjectsSyncWorkerType.*
import com.simprints.id.services.sync.events.master.models.SubjectsSyncWorkerType.Companion.tagForType
import com.simprints.id.services.sync.events.master.workers.SubjectsStartSyncReporterWorker.Companion.SYNC_ID_STARTED
import com.simprints.id.services.sync.events.up.workers.extractUpSyncProgress
import com.simprints.id.services.sync.events.up.workers.getUpCountsFromOutput
import timber.log.Timber

class EventSyncStateProcessorImpl(val ctx: Context,
                                  val personRepository: SubjectRepository,
                                  private val eventSyncCache: EventSyncCache,
                                  private val syncWorkersLiveDataProvider: SyncWorkersLiveDataProvider = SyncWorkersLiveDataProviderImpl(ctx)) : SubjectsSyncStateProcessor {

    override fun getLastSyncState(): LiveData<SubjectsSyncState> =
        observerForLastSyncId().switchMap { lastSyncId ->
            observerForLastSyncIdWorkers(lastSyncId).switchMap { syncWorkers ->
                MutableLiveData<SubjectsSyncState>().apply {
                    with(syncWorkers) {
                        val progress = calculateProgressForDownSync() + calculateProgressForUpSync()
                        val total = calculateTotalForSync()

                        val upSyncStates = upSyncUploadersStates() + upSyncCountersStates()
                        val downSyncStates = downSyncDownloadersStates() + downSyncCountersStates()

                        val syncState = SubjectsSyncState(lastSyncId, progress, total, upSyncStates, downSyncStates)
                        this@apply.postValue(syncState)
                        Timber.tag(SYNC_LOG_TAG).d("[PROCESSOR] Emitting for UI $syncState")
                    }
                }
            }
        }


    private fun observerForLastSyncId(): LiveData<String> {
        return syncWorkersLiveDataProvider.getStartSyncReportersLiveData().switchMap { startSyncReporters ->
            Timber.tag(SYNC_LOG_TAG).d("[PROCESSOR] Received updated from Master Scheduler")

            val completedSyncMaster = startSyncReporters.completedWorkers()
            val mostRecentSyncMaster = completedSyncMaster.sortByScheduledTime().lastOrNull()

            MutableLiveData<String>().apply {
                if (mostRecentSyncMaster != null) {
                    val lastSyncId = mostRecentSyncMaster.outputData.getString(SYNC_ID_STARTED)
                    if (!lastSyncId.isNullOrBlank()) {
                        Timber.tag(SYNC_LOG_TAG).d("[PROCESSOR] Received sync id: $lastSyncId")
                        this.postValue(lastSyncId)
                    }
                }
            }
        }
    }

    private fun observerForLastSyncIdWorkers(lastSyncId: String) =
        syncWorkersLiveDataProvider.getSyncWorkersLiveData(lastSyncId)

    private fun List<WorkInfo>.completedWorkers() =
        this.filter { it.state == WorkInfo.State.SUCCEEDED }

    private fun List<WorkInfo>.calculateTotalForSync(): Int? {
        val totalDown = calculateTotalForDownSync()
        val totalUp = calculateTotalForUpSync()
        return if (totalUp != null && totalDown != null) {
            totalUp + totalDown
        } else {
            null
        }
    }

    private fun List<WorkInfo>.calculateTotalForDownSync(): Int? {
        val countersCompleted = this.filterByTags(tagForType(DOWN_COUNTER)).completedWorkers()
        val counter = countersCompleted.firstOrNull()
        return counter?.getDownCountsFromOutput()?.sumBy { it.count }
    }

    private fun List<WorkInfo>.calculateTotalForUpSync(): Int? {
        val countersCompleted = this.filterByTags(tagForType(UP_COUNTER)).completedWorkers()
        val counter = countersCompleted.firstOrNull()
        return counter?.getUpCountsFromOutput()
    }

    private fun List<WorkInfo>.upSyncUploadersStates(): List<SyncWorkerInfo> =
        filterByTags(tagForType(UPLOADER)).map { SyncWorkerInfo(UPLOADER, fromWorkInfo(it.state, it.didFailBecauseCloudIntegration())) }

    private fun List<WorkInfo>.downSyncDownloadersStates(): List<SyncWorkerInfo> =
        filterByTags(tagForType(DOWNLOADER)).map { SyncWorkerInfo(DOWNLOADER, fromWorkInfo(it.state, it.didFailBecauseCloudIntegration())) }

    private fun List<WorkInfo>.downSyncCountersStates(): List<SyncWorkerInfo> =
        filterByTags(tagForType(DOWN_COUNTER)).map { SyncWorkerInfo(DOWN_COUNTER, fromWorkInfo(it.state, it.didFailBecauseCloudIntegration())) }

    private fun List<WorkInfo>.upSyncCountersStates(): List<SyncWorkerInfo> =
        filterByTags(tagForType(UP_COUNTER)).map { SyncWorkerInfo(UP_COUNTER, fromWorkInfo(it.state, it.didFailBecauseCloudIntegration())) }

    private fun List<WorkInfo>.calculateProgressForDownSync(): Int {
        val downWorkers = this.filterByTags(tagForType(DOWNLOADER))
        val progresses = downWorkers.map { worker ->
            worker.extractDownSyncProgress(eventSyncCache)
        }

        return progresses.filterNotNull().sum()
    }

    private fun List<WorkInfo>.calculateProgressForUpSync(): Int {
        val upWorkers = this.filterByTags(tagForType(UPLOADER))
        val progresses = upWorkers.map { worker ->
            worker.extractUpSyncProgress(eventSyncCache)
        }

        return progresses.filterNotNull().sum()
    }
}
