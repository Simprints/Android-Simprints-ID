package com.simprints.id.services.scheduledSync.people.master

import android.content.Context
import androidx.work.*
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.people.down.controllers.DownSyncWorkerBuilder
import com.simprints.id.services.scheduledSync.people.down.models.PeopleDownSyncTrigger
import com.simprints.id.services.scheduledSync.people.up.controllers.builder.UpSyncWorkersBuilder
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SyncMasterWorker(private val appContext: Context,
                       params: WorkerParameters) : SimCoroutineWorker(appContext, params) {

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var downSyncWorkerBuilder: DownSyncWorkerBuilder
    @Inject lateinit var upSyncWorkerBuilder: UpSyncWorkersBuilder
    @Inject lateinit var preferenceManager: PreferencesManager

    companion object {
        const val MIN_BACKOFF_MILLIS = 15L //15 seconds

        const val MASTER_SYNC_SCHEDULERS = "MASTER_SYNC_SCHEDULERS"
        const val MASTER_SYNC_SCHEDULER_ONE_TIME = "MASTER_SYNC_SCHEDULER_ONE_TIME"
        const val MASTER_SYNC_SCHEDULER_PERIODIC_TIME = "MASTER_SYNC_SCHEDULER_PERIODIC_TIME"

        const val TAG_SYNC_WORKERS = "TAG_SYNC_WORKERS"
        const val TAG_LAST_SYNC_ID = "TAG_LAST_SYNC_ID_"
        const val TAG_SCHEDULED_AT = "TAG_SCHEDULED_AT_"

        const val OUTPUT_LAST_SYNC_ID = "OUTPUT_LAST_SYNC_ID"
    }

    private val wm: WorkManager
        get() = WorkManager.getInstance(appContext)

    private val downSyncWorkers
        get() = wm.getWorkInfosByTag(TAG_SYNC_WORKERS).get().apply {
            this.sortBy { it -> it.tags.first { it.contains(TAG_SCHEDULED_AT) } }
        }

    private val isPeriodicMasterWorker
        get() = tags.contains(MASTER_SYNC_SCHEDULER_PERIODIC_TIME)

    override suspend fun doWork(): Result {
        return try {
            getComponent<SyncMasterWorker> { it.inject(this) }
            Timber.d("Sync - started")

            return if (!isSyncRunning()) {
                val uniqueSyncID = generateUniqueDownSyncId()
                val chain = upSyncWorkersChain(uniqueSyncID) + downSyncWorkersChain(uniqueSyncID)
                wm.enqueue(chain)

                Timber.d("Sync - completed: $uniqueSyncID")
                resultSetter.success(workDataOf(OUTPUT_LAST_SYNC_ID to "$uniqueSyncID"))
            } else {
                val lastSyncId = getLastSyncId()

                Timber.d("Sync - completed: $lastSyncId")
                resultSetter.success(workDataOf(OUTPUT_LAST_SYNC_ID to lastSyncId))
            }.also {
                clearWorkerHistory()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Timber.d("Sync - failed")
            resultSetter.failure()
        }
    }

    private fun generateUniqueDownSyncId() = UUID.randomUUID().toString()

    private suspend fun downSyncWorkersChain(uniqueSyncID: String): List<WorkRequest> =
        if (preferenceManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.PERIODIC_BACKGROUND] == false && isPeriodicMasterWorker) {
            emptyList()
        } else {
            downSyncWorkerBuilder.buildDownSyncWorkerChain(uniqueSyncID)
        }

    private fun upSyncWorkersChain(uniqueSyncID: String): List<WorkRequest> =
        upSyncWorkerBuilder.buildUpSyncWorkerChain(uniqueSyncID)

    private fun clearWorkerHistory() {
        val downSyncWorkersTags = downSyncWorkers.filter { it.state != WorkInfo.State.CANCELLED }.map { it.tags }.flatten().distinct()
        val syncIds = downSyncWorkersTags.filter { it.contains(TAG_LAST_SYNC_ID) }
        if (syncIds.size > 1) {
            val toKeep = syncIds.takeLast(1)
            val toRemove = syncIds - toKeep
            toRemove.forEach { wm.cancelAllWorkByTag(it) }
        }

        val downSyncWorkersWithoutSyncId = downSyncWorkers.filter { getTagWithSyncId(it.tags) == null && it.state != WorkInfo.State.CANCELLED }
        downSyncWorkersWithoutSyncId.forEach { wm.cancelWorkById(it.id) }
    }


    private fun getLastSyncId(): String? {
        val lastDownSyncWorker = downSyncWorkers?.findLast { getTagWithSyncId(it.tags) != null }
        return getTagWithSyncId(lastDownSyncWorker?.tags).removePrefix(TAG_LAST_SYNC_ID)
    }

    private fun getTagWithSyncId(tags: Set<String>?) =
        tags?.firstOrNull { it.contains(TAG_LAST_SYNC_ID) }


    private fun isSyncRunning(): Boolean = !getWorkInfoForRunningSyncWorkers().isNullOrEmpty()

    private fun getWorkInfoForRunningSyncWorkers(): List<WorkInfo>? {
        return downSyncWorkers?.filter { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
    }
}
