package com.simprints.id.services.scheduledSync.people.master

import android.content.Context
import androidx.work.*
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder
import java.util.*
import javax.inject.Inject

class PeopleSyncMasterWorker(private val appContext: Context,
                             params: WorkerParameters) : SimCoroutineWorker(appContext, params) {

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var downSyncWorkerBuilder: PeopleDownSyncWorkersBuilder
    @Inject lateinit var upSyncWorkerBuilder: PeopleUpSyncWorkersBuilder
    @Inject lateinit var preferenceManager: PreferencesManager

    companion object {
        const val MIN_BACKOFF_MILLIS = 15L //15 seconds

        const val MASTER_SYNC_SCHEDULERS = "MASTER_SYNC_SCHEDULERS"
        const val MASTER_SYNC_SCHEDULER_ONE_TIME = "MASTER_SYNC_SCHEDULER_ONE_TIME"
        const val MASTER_SYNC_SCHEDULER_PERIODIC_TIME = "MASTER_SYNC_SCHEDULER_PERIODIC_TIME"

        const val TAG_PEOPLE_SYNC_ALL_WORKERS = "TAG_PEOPLE_SYNC_ALL_WORKERS"
        const val TAG_MASTER_SYNC_ID = "TAG_MASTER_SYNC_ID_"
        const val TAG_SCHEDULED_AT = "TAG_SCHEDULED_AT_"

        const val OUTPUT_LAST_SYNC_ID = "OUTPUT_LAST_SYNC_ID"
    }

    private val wm: WorkManager
        get() = WorkManager.getInstance(appContext)

    private val syncWorkers
        get() = wm.getWorkInfosByTag(TAG_PEOPLE_SYNC_ALL_WORKERS).get().apply {
            if (this.isNullOrEmpty()) {
                this.sortBy { it -> it.tags.first { it.contains(TAG_SCHEDULED_AT) } }
            }
        }

    val uniqueSyncId by lazy {
        UUID.randomUUID().toString()
    }

    private val isPeriodicMasterWorker
        get() = tags.contains(MASTER_SYNC_SCHEDULER_PERIODIC_TIME)

    override suspend fun doWork(): Result {
        return try {
            getComponent<PeopleSyncMasterWorker> { it.inject(this) }
            crashlyticsLog("Preparing master work")

            return if (!isSyncRunning()) {
                val chain = upSyncWorkersChain(uniqueSyncId) + downSyncWorkersChain(uniqueSyncId)
                wm.enqueue(chain)

                logSuccess("Master work done: new id $uniqueSyncId")
                resultSetter.success(workDataOf(OUTPUT_LAST_SYNC_ID to uniqueSyncId))
            } else {
                val lastSyncId = getLastSyncId()

                logSuccess("Master work done: id already exists $lastSyncId")
                resultSetter.success(workDataOf(OUTPUT_LAST_SYNC_ID to lastSyncId))
            }.also {
                clearWorkerHistory()
            }
        } catch (t: Throwable) {
            logFailure(t)
            resultSetter.failure()
        }
    }

    private suspend fun downSyncWorkersChain(uniqueSyncID: String): List<WorkRequest> =
        if (preferenceManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.PERIODIC_BACKGROUND] == false && isPeriodicMasterWorker) {
            emptyList()
        } else {
            downSyncWorkerBuilder.buildDownSyncWorkerChain(uniqueSyncID)
        }

    private fun upSyncWorkersChain(uniqueSyncID: String): List<WorkRequest> =
        upSyncWorkerBuilder.buildUpSyncWorkerChain(uniqueSyncID)

    private fun clearWorkerHistory() {
        val downSyncWorkersTags = syncWorkers.filter { it.state != WorkInfo.State.CANCELLED }.map { it.tags }.flatten().distinct()
        val syncIds = downSyncWorkersTags.filter { it.contains(TAG_MASTER_SYNC_ID) }
        if (syncIds.size > 1) {
            val toKeep = syncIds.takeLast(1)
            val toRemove = syncIds - toKeep
            toRemove.forEach { wm.cancelAllWorkByTag(it) }
        }

        val syncWorkersWithoutSyncId = syncWorkers.filter { getTagWithSyncId(it.tags) == null && it.state != WorkInfo.State.CANCELLED }
        syncWorkersWithoutSyncId.forEach { wm.cancelWorkById(it.id) }
    }


    private fun getLastSyncId(): String? {
        val lastSyncWorker = syncWorkers?.findLast { getTagWithSyncId(it.tags) != null }
        return getTagWithSyncId(lastSyncWorker?.tags)?.removePrefix(TAG_MASTER_SYNC_ID)
    }

    private fun getTagWithSyncId(tags: Set<String>?) =
        tags?.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }

    private fun isSyncRunning(): Boolean = !getWorkInfoForRunningSyncWorkers().isNullOrEmpty()

    private fun getWorkInfoForRunningSyncWorkers(): List<WorkInfo>? {
        return syncWorkers?.filter { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
    }

    private fun logFailure(t: Throwable) =
        logFailure<PeopleSyncMasterWorker>(t)

    private fun logSuccess(message: String) =
        logSuccess<PeopleSyncMasterWorker>(message)

    private fun crashlyticsLog(message: String) =
        crashlyticsLog<PeopleSyncMasterWorker>(message)
}
