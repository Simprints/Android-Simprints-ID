package com.simprints.id.services.scheduledSync.people.master.workers

import android.content.Context
import androidx.work.*
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncTrigger
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncWorkerType.LAST_SYNC_REPORTER
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleLastSyncReporterWorker.Companion.SYNC_ID_TO_MARK_AS_COMPLETED
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder
import java.util.*
import javax.inject.Inject

class PeopleSyncMasterWorker(private val appContext: Context,
                             params: WorkerParameters) : SimCoroutineWorker(appContext, params) {

    companion object {
        const val MIN_BACKOFF_SECS = 15L

        const val MASTER_SYNC_SCHEDULERS = "MASTER_SYNC_SCHEDULERS"
        const val MASTER_SYNC_SCHEDULER_ONE_TIME = "MASTER_SYNC_SCHEDULER_ONE_TIME"
        const val MASTER_SYNC_SCHEDULER_PERIODIC_TIME = "MASTER_SYNC_SCHEDULER_PERIODIC_TIME"

        const val TAG_PEOPLE_SYNC_ALL_WORKERS = "TAG_PEOPLE_SYNC_ALL_WORKERS"
        const val TAG_MASTER_SYNC_ID = "TAG_MASTER_SYNC_ID_"
        const val TAG_SCHEDULED_AT = "TAG_SCHEDULED_AT_"

        const val OUTPUT_LAST_SYNC_ID = "OUTPUT_LAST_SYNC_ID"
    }

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var downSyncWorkerBuilder: PeopleDownSyncWorkersBuilder
    @Inject lateinit var upSyncWorkerBuilder: PeopleUpSyncWorkersBuilder
    @Inject lateinit var preferenceManager: PreferencesManager
    @Inject lateinit var peopleSyncCache: PeopleSyncCache

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

    private val isOneTimeMasterWorker
        get() = tags.contains(MASTER_SYNC_SCHEDULER_ONE_TIME)

    override suspend fun doWork(): Result {
        return try {
            getComponent<PeopleSyncMasterWorker> { it.inject(this) }
            crashlyticsLog("Preparing master work")

            return if (!isSyncRunning()) {
                val chain = upSyncWorkersChain(uniqueSyncId) + downSyncWorkersChain(uniqueSyncId)
                wm.beginWith(chain).then(lastSyncWorker(uniqueSyncId)).enqueue()

                peopleSyncCache.clearProgresses()
                logSuccess("Master work done: new id $uniqueSyncId")
                clearWorkerHistory(uniqueSyncId)
                resultSetter.success(workDataOf(OUTPUT_LAST_SYNC_ID to uniqueSyncId))
            } else {
                val lastSyncId = getLastSyncId()

                logSuccess("Master work done: id already exists $lastSyncId")
                resultSetter.success(workDataOf(OUTPUT_LAST_SYNC_ID to lastSyncId))
            }
        } catch (t: Throwable) {
            logFailure(t)
            resultSetter.failure()
        }
    }

    private fun lastSyncWorker(uniqueSyncID: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(PeopleLastSyncReporterWorker::class.java)
            .addTag("${TAG_MASTER_SYNC_ID}${uniqueSyncID}")
            .addTag("${TAG_SCHEDULED_AT}${Date().time}")
            .addTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
            .addTag(tagForType(LAST_SYNC_REPORTER))
            .setInputData(workDataOf(SYNC_ID_TO_MARK_AS_COMPLETED to uniqueSyncID))
            .build()

    private suspend fun downSyncWorkersChain(uniqueSyncID: String): List<OneTimeWorkRequest> {
        val backgroundOnForPeriodicSync = preferenceManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.PERIODIC_BACKGROUND] == true
        val downSyncChainRequired = isOneTimeMasterWorker || backgroundOnForPeriodicSync

        return if (downSyncChainRequired) {
            downSyncWorkerBuilder.buildDownSyncWorkerChain(uniqueSyncID)
        } else {
            emptyList()
        }
    }

    private fun upSyncWorkersChain(uniqueSyncID: String): List<OneTimeWorkRequest> =
        upSyncWorkerBuilder.buildUpSyncWorkerChain(uniqueSyncID)

    private fun clearWorkerHistory(uniqueId: String) {
        val otherDownSyncWorkers = syncWorkers.filter { !it.tags.contains("$TAG_MASTER_SYNC_ID$uniqueId") }
        val syncWorkersWithoutSyncId = syncWorkers.filter { getTagWithSyncId(it.tags) == null && it.state != WorkInfo.State.CANCELLED }
        (otherDownSyncWorkers + syncWorkersWithoutSyncId).forEach { wm.cancelWorkById(it.id) }
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
        crashReportLog<PeopleSyncMasterWorker>(message)
}
