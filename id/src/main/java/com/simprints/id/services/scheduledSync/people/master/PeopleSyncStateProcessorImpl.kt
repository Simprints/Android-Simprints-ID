package com.simprints.id.services.scheduledSync.people.master

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.common.models.fromDownSync
import com.simprints.id.data.db.common.models.totalCount
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.services.scheduledSync.people.down.workers.extractDownSyncProgress
import com.simprints.id.services.scheduledSync.people.down.workers.getDownCountsFromOutput
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULERS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.OUTPUT_LAST_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_SCHEDULED_AT
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncState.WorkerState
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.*
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.people.up.workers.extractUpSyncProgress
import com.simprints.id.services.scheduledSync.people.up.workers.getUpCountsFromOutput
import timber.log.Timber

class PeopleSyncStateProcessorImpl(val ctx: Context,
                                   val personRepository: PersonRepository) : PeopleSyncStateProcessor {

    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

    override fun getLastSyncState(): LiveData<PeopleSyncState> =
        observerForLastDowSyncId().switchMap { lastSyncId ->
            observerForLastSyncIdWorkers(lastSyncId).switchMap { syncWorkers ->
                Timber.d("Sync - Received info for $lastSyncId: ${syncWorkers?.map { it.tags }}}")

                MutableLiveData<PeopleSyncState>().apply {
                    with(syncWorkers) {
                        val progress = calculateProgressForDownSync() + calculateProgressForUpSync()
                        val total = calculateTotalForDownSync() + calculateTotalForUpSync()
                        val upSyncStates = uploadersStates() + upSyncCountersStates()
                        val downSyncStates = downloadersStates() + downSyncCountersStates()

                        val syncState = PeopleSyncState(lastSyncId, progress, total, upSyncStates, downSyncStates)
                        this@apply.postValue(syncState)
                        Timber.d("Sync - Emitting ${JsonHelper.toJson(syncState)}")
                    }
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

    private fun List<WorkInfo>.calculateTotalForDownSync(): Int {
        val countersCompleted = this.filterByTags(tagForType(DOWN_COUNTER)).completedWorkers()
        val counter = countersCompleted.firstOrNull()
        return counter?.getDownCountsFromOutput()?.sumBy { it.fromDownSync() } ?: -1
    }

    private fun List<WorkInfo>.calculateTotalForUpSync(): Int {
        val countersCompleted = this.filterByTags(tagForType(UP_COUNTER)).completedWorkers()
        val counter = countersCompleted.firstOrNull()
        return counter?.getUpCountsFromOutput()?.totalCount() ?: -1
    }

    private fun List<WorkInfo>.uploadersStates(): List<WorkerState> =
        this.filterByTags(tagForType(UPLOADER)).groupBy { it }.values.flatten().map { WorkerState(UPLOADER, it.state) }

    private fun List<WorkInfo>.downloadersStates(): List<WorkerState> =
        this.filterByTags(tagForType(DOWNLOADER)).groupBy { it }.values.flatten().map { WorkerState(DOWNLOADER, it.state) }

    private fun List<WorkInfo>.downSyncCountersStates(): List<WorkerState> =
        this.filterByTags(tagForType(DOWN_COUNTER)).groupBy { it }.values.flatten().map { WorkerState(DOWN_COUNTER, it.state) }

    private fun List<WorkInfo>.upSyncCountersStates(): List<WorkerState> =
        this.filterByTags(tagForType(UP_COUNTER)).groupBy { it }.values.flatten().map { WorkerState(UP_COUNTER, it.state) }

    private fun List<WorkInfo>.calculateProgressForDownSync(): Int {
        val downWorkers = this.filterByTags(tagForType(DOWNLOADER)).groupBy { it.id }
        val progresses = downWorkers.mapNotNull {
            val infos = it.value
            val maxWorker = infos.maxBy { it.extractDownSyncProgress() ?: 0 }
            maxWorker?.extractDownSyncProgress()
        }
        return progresses.sum()
    }

    private fun List<WorkInfo>.calculateProgressForUpSync(): Int {
        val upWorkers = this.filterByTags(tagForType(UPLOADER)).groupBy { it.id }
        val progresses = upWorkers.map {
            val infos = it.value
            val maxWorker = infos.maxBy { it.extractUpSyncProgress() ?: 0 }
            maxWorker?.extractUpSyncProgress() ?: 0
        }
        return progresses.sum()
    }

    private fun List<WorkInfo>.filterByTags(vararg tagsToFilter: String) =
        this.filter { it.tags.firstOrNull { tagsToFilter.contains(it) } != null }.sortedBy { it ->
            it.tags.first { it.contains(TAG_SCHEDULED_AT) }
        }
}
