package com.simprints.id.services.scheduledSync.people.master

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simprints.id.data.db.people_sync.down.domain.totalCount
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.services.scheduledSync.people.down.workers.extractDownSyncProgress
import com.simprints.id.services.scheduledSync.people.down.workers.getDownCountsFromOutput
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULERS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.OUTPUT_LAST_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_SCHEDULED_AT
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.*
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.people.up.workers.extractUpSyncProgress
import timber.log.Timber

class PeopleSyncStateProcessorImpl(val ctx: Context,
                                   val personRepository: PersonRepository) : PeopleSyncStateProcessor {

    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

    //STopShip: the total number of patients to Upload can change while the Uploader progress
    private val totalToUpload = personRepository.count(PersonLocalDataSource.Query(toSync = true))

    override fun getLastSyncState(): LiveData<PeopleSyncState> =
        observerForLastDowSyncId().switchMap { lastSyncId ->
            observerForLastSyncIdWorkers(lastSyncId).switchMap { syncWorkers ->
                Timber.d("Sync - Received info for $lastSyncId: ${syncWorkers?.map { it.tags }}}")

                MutableLiveData<PeopleSyncState>().apply {
                    val progress = syncWorkers.calculateProgressForDownSync() + syncWorkers.calculateProgressForUpSync()
                    val total = syncWorkers.calculateTotal()
                    val upSyncStates = syncWorkers.upSyncStates()
                    val downSyncStates = syncWorkers.downSyncStates() + syncWorkers.downCountSyncStates()

                    val syncState = PeopleSyncState(lastSyncId, progress, total, upSyncStates, downSyncStates)
                    this.postValue(syncState)
                    Timber.d("Sync - Emitting $syncState")
                }
            }
        }


    private fun observerForLastDowSyncId(): LiveData<String> {
        return wm.getWorkInfosByTagLiveData(MASTER_SYNC_SCHEDULERS).switchMap { masterWorkers ->
            Timber.d("Sync - Received info for schedulers: ${masterWorkers.map { it.tags }}")

            val completedSyncMaster = masterWorkers.completedWorkers()
            MutableLiveData<String>().apply {
                if (completedSyncMaster.isNotEmpty()) {
                    val lastSyncId = completedSyncMaster.mapNotNull { it.outputData.getString(OUTPUT_LAST_SYNC_ID) }.firstOrNull()
                    if (!lastSyncId.isNullOrBlank()) {
                        this.postValue(lastSyncId)
                    }
                }
            }
        }
    }

    private fun observerForLastSyncIdWorkers(lastSyncId: String) =
        wm.getWorkInfosByTagLiveData("${TAG_MASTER_SYNC_ID}${lastSyncId}")

    private fun List<WorkInfo>.completedWorkers() =
        this.filter { it.state == WorkInfo.State.SUCCEEDED }

    private fun List<WorkInfo>.calculateTotal(): Int? {
        val countersCompleted = this.filterByTags(tagForType(COUNTER)).completedWorkers()
        val counter = countersCompleted.firstOrNull()
        val totalToDownload = counter?.getDownCountsFromOutput()?.sumBy { it.totalCount() }
        return totalToDownload?.let {
            it + totalToUpload
        }
    }

    private fun List<WorkInfo>.upSyncStates(): List<PeopleSyncState.WorkerState> =
        this.filterByTags(tagForType(UPLOADER)).groupBy { it }.values.flatten().map { PeopleSyncState.WorkerState(UPLOADER, it.state) }

    private fun List<WorkInfo>.downSyncStates(): List<PeopleSyncState.WorkerState> =
        this.filterByTags(tagForType(DOWNLOADER)).groupBy { it }.values.flatten().map { PeopleSyncState.WorkerState(DOWNLOADER, it.state) }

    private fun List<WorkInfo>.downCountSyncStates(): List<PeopleSyncState.WorkerState> =
        this.filterByTags(tagForType(COUNTER)).groupBy { it }.values.flatten().map { PeopleSyncState.WorkerState(COUNTER, it.state) }

    private fun List<WorkInfo>.calculateProgressForDownSync(): Int {
        val downWorkers = this.filterByTags(tagForType(DOWNLOADER)).groupBy { it.id }
        val progresses = downWorkers.map {
            val infos = it.value
            val maxWorker = infos.maxBy { it.extractDownSyncProgress() }
            maxWorker?.extractDownSyncProgress() ?: 0
        }
        return progresses.sum()
    }

    private fun List<WorkInfo>.calculateProgressForUpSync(): Int {
        val upWorkers = this.filterByTags(tagForType(UPLOADER)).groupBy { it.id }
        val progresses = upWorkers.map {
            val infos = it.value
            val maxWorker = infos.maxBy { it.extractUpSyncProgress() }
            maxWorker?.extractUpSyncProgress() ?: 0
        }
        return progresses.sum()
    }

    private fun List<WorkInfo>.filterByTags(vararg tagsToFilter: String) =
        this.filter { it.tags.firstOrNull { tagsToFilter.contains(it) } != null }.sortedBy { it ->
            it.tags.first { it.contains(TAG_SCHEDULED_AT) }
        }
}
