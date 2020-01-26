package com.simprints.id.services.scheduledSync.people.master.workers

import android.content.Context
import androidx.work.*
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.common.*
import com.simprints.id.services.scheduledSync.people.down.controllers.PeopleDownSyncWorkersBuilder
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncTrigger
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleLastSyncReporterWorker.Companion.SYNC_ID_TO_MARK_AS_COMPLETED
import com.simprints.id.services.scheduledSync.people.up.controllers.PeopleUpSyncWorkersBuilder
import java.util.*
import javax.inject.Inject

open class PeopleSyncMasterWorker(private val appContext: Context,
                                  params: WorkerParameters) : SimCoroutineWorker(appContext, params) {

    companion object {
        const val MIN_BACKOFF_SECS = 15L

        const val MASTER_SYNC_SCHEDULERS = "MASTER_SYNC_SCHEDULERS"
        const val MASTER_SYNC_SCHEDULER_ONE_TIME = "MASTER_SYNC_SCHEDULER_ONE_TIME"
        const val MASTER_SYNC_SCHEDULER_PERIODIC_TIME = "MASTER_SYNC_SCHEDULER_PERIODIC_TIME"

        const val OUTPUT_LAST_SYNC_ID = "OUTPUT_LAST_SYNC_ID"
    }

    override val tag: String = PeopleSyncMasterWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var downSyncWorkerBuilder: PeopleDownSyncWorkersBuilder
    @Inject lateinit var upSyncWorkerBuilder: PeopleUpSyncWorkersBuilder
    @Inject lateinit var preferenceManager: PreferencesManager
    @Inject lateinit var peopleSyncCache: PeopleSyncCache

    private val wm: WorkManager
        get() = WorkManager.getInstance(appContext)

    private val syncWorkers
        get() = wm.getAllPeopleSyncWorkersInfo().get().apply {
            if (this.isNullOrEmpty()) {
                this.sortByScheduledTime()
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
            crashlyticsLog("Start")

            if (!isSyncRunning()) {
                val upSyncWorkers = upSyncWorkersChain(uniqueSyncId)
                val downSyncWorkers =  downSyncWorkersChain(uniqueSyncId)
                val chain = upSyncWorkers + downSyncWorkers
                wm.beginWith(chain).then(lastSyncWorker(uniqueSyncId)).enqueue()

                peopleSyncCache.clearProgresses()
                clearWorkerHistory(uniqueSyncId)
                success(workDataOf(OUTPUT_LAST_SYNC_ID to uniqueSyncId),
                    "Master work done: new id $uniqueSyncId")
            } else {
                val lastSyncId = getLastSyncId()

                success(workDataOf(OUTPUT_LAST_SYNC_ID to lastSyncId),
                    "Master work done: id already exists $lastSyncId")
            }
        } catch (t: Throwable) {
            fail(t)
        }
    }

    private fun lastSyncWorker(uniqueSyncID: String): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(PeopleLastSyncReporterWorker::class.java)
            .addTagForMasterSyncId(uniqueSyncID)
            .addTagForScheduledAtNow()
            .addCommonTagForAllSyncWorkers()
            .addTagForLastSyncReporter()
            .setInputData(workDataOf(SYNC_ID_TO_MARK_AS_COMPLETED to uniqueSyncID))
            .build() as OneTimeWorkRequest

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
        val otherDownSyncWorkers = syncWorkers.filter { !it.isPartOfPeopleSync(uniqueId) }
        val syncWorkersWithoutSyncId = syncWorkers.filter { it.getUniqueSyncId() == null && it.state != WorkInfo.State.CANCELLED }
        (otherDownSyncWorkers + syncWorkersWithoutSyncId).forEach { wm.cancelWorkById(it.id) }
    }


    private fun getLastSyncId(): String? {
        return syncWorkers.last()?.getUniqueSyncId()
    }

    private fun isSyncRunning(): Boolean = !getWorkInfoForRunningSyncWorkers().isNullOrEmpty()

    private fun getWorkInfoForRunningSyncWorkers(): List<WorkInfo>? {
        return syncWorkers?.filter { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
    }
}
