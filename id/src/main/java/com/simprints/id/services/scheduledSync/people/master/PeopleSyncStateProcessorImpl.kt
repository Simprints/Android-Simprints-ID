package com.simprints.id.services.scheduledSync.people.master

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.work.WorkInfo
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.common.models.fromDownSync
import com.simprints.id.data.db.common.models.totalCount
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.services.scheduledSync.people.down.workers.extractDownSyncProgress
import com.simprints.id.services.scheduledSync.people.down.workers.getDownCountsFromOutput
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncProgressCache
import com.simprints.id.services.scheduledSync.people.master.internal.SyncWorkersLiveDataProvider
import com.simprints.id.services.scheduledSync.people.master.internal.SyncWorkersLiveDataProviderImpl
import com.simprints.id.services.scheduledSync.people.master.internal.didFailBecauseCloudIntegration
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState.SyncWorkerInfo
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType.*
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerState.Companion.fromWorkInfo
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.OUTPUT_LAST_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.TAG_SCHEDULED_AT
import com.simprints.id.services.scheduledSync.people.up.workers.extractUpSyncProgress
import com.simprints.id.services.scheduledSync.people.up.workers.getUpCountsFromOutput
import timber.log.Timber

class PeopleSyncStateProcessorImpl(val ctx: Context,
                                   val personRepository: PersonRepository,
                                   private val progressCache: PeopleSyncProgressCache,
                                   private val syncWorkersLiveDataProvider: SyncWorkersLiveDataProvider = SyncWorkersLiveDataProviderImpl(ctx)) : PeopleSyncStateProcessor {

    override fun getLastSyncState(): LiveData<PeopleSyncState> =
        observerForLastSyncId().switchMap { lastSyncId ->
            observerForLastSyncIdWorkers(lastSyncId).switchMap { syncWorkers ->
                MutableLiveData<PeopleSyncState>().apply {
                    with(syncWorkers) {
                        val progress = calculateProgressForDownSync() + calculateProgressForUpSync()
                        val total = calculateTotalForSync()

                        val upSyncStates = upSyncUploadersStates() + upSyncCountersStates()
                        val downSyncStates = downSyncDownloadersStates() + downSyncCountersStates()

                        val syncState = PeopleSyncState(lastSyncId, progress, total, upSyncStates, downSyncStates)
                        this@apply.postValue(syncState)
                        Timber.d("I/SYNC Emitting ${JsonHelper.toJson(syncState)}")
                    }
                }
            }
        }


    private fun observerForLastSyncId(): LiveData<String> {
        return syncWorkersLiveDataProvider.getMasterWorkersLiveData().switchMap { masterWorkers ->
            Timber.d("I/SYNC Update from MASTER_SYNC_SCHEDULERS}")

            val completedSyncMaster = masterWorkers.completedWorkers()
            MutableLiveData<String>().apply {
                if (completedSyncMaster.isNotEmpty()) {
                    val lastSyncId = completedSyncMaster.mapNotNull { it.outputData.getString(OUTPUT_LAST_SYNC_ID) }.firstOrNull()
                    if (!lastSyncId.isNullOrBlank()) {
                        Timber.d("I/SYNC Last sync id $lastSyncId}")
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
        return counter?.getDownCountsFromOutput()?.sumBy { it.fromDownSync() }
    }

    private fun List<WorkInfo>.calculateTotalForUpSync(): Int? {
        val countersCompleted = this.filterByTags(tagForType(UP_COUNTER)).completedWorkers()
        val counter = countersCompleted.firstOrNull()
        return counter?.getUpCountsFromOutput()?.totalCount()
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
            worker.extractDownSyncProgress(progressCache)
        }

        return progresses.filterNotNull().sum()
    }

    private fun List<WorkInfo>.calculateProgressForUpSync(): Int {
        val upWorkers = this.filterByTags(tagForType(UPLOADER))
        val progresses = upWorkers.map { worker ->
            worker.extractUpSyncProgress(progressCache)
        }

        return progresses.filterNotNull().sum()
    }

    private fun List<WorkInfo>.filterByTags(vararg tagsToFilter: String) =
        this.filter {
            it.tags.firstOrNull { tag ->
                tagsToFilter.contains(tag)
            } != null
        }.sortedBy { it ->
            it.tags.first { it.contains(TAG_SCHEDULED_AT) }
        }
}
