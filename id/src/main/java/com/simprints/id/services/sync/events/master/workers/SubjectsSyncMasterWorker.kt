package com.simprints.id.services.sync.events.master.workers

import android.content.Context
import androidx.work.*
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.events.common.*
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilder
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.models.SubjectsDownSyncSetting.EXTRA
import com.simprints.id.services.sync.events.master.models.SubjectsDownSyncSetting.ON
import com.simprints.id.services.sync.events.up.SubjectsUpSyncWorkersBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

open class SubjectsSyncMasterWorker(private val appContext: Context,
                                    params: WorkerParameters) : SimCoroutineWorker(appContext, params) {

    companion object {
        const val MIN_BACKOFF_SECS = 15L

        const val MASTER_SYNC_SCHEDULERS = "MASTER_SYNC_SCHEDULERS"
        const val MASTER_SYNC_SCHEDULER_ONE_TIME = "MASTER_SYNC_SCHEDULER_ONE_TIME"
        const val MASTER_SYNC_SCHEDULER_PERIODIC_TIME = "MASTER_SYNC_SCHEDULER_PERIODIC_TIME"

        const val OUTPUT_LAST_SYNC_ID = "OUTPUT_LAST_SYNC_ID"
    }

    override val tag: String = SubjectsSyncMasterWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var downSyncWorkerBuilder: EventDownSyncWorkersBuilder
    @Inject lateinit var upSyncWorkerBuilder: SubjectsUpSyncWorkersBuilder
    @Inject lateinit var preferenceManager: PreferencesManager
    @Inject lateinit var eventSyncCache: EventSyncCache
    @Inject lateinit var subjectsSyncSubMasterWorkersBuilder: SubjectsSyncSubMasterWorkersBuilder

    private val wm: WorkManager
        get() = WorkManager.getInstance(appContext)

    private val syncWorkers
        get() = wm.getAllSubjectsSyncWorkersInfo().get().apply {
            if (this.isNullOrEmpty()) {
                this.sortByScheduledTime()
            }
        }

    val uniqueSyncId by lazy {
        UUID.randomUUID().toString()
    }

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                getComponent<SubjectsSyncMasterWorker> { it.inject(this@SubjectsSyncMasterWorker) }
                crashlyticsLog("Start")

                if (!isSyncRunning()) {
                    val startSyncReporterWorker = subjectsSyncSubMasterWorkersBuilder.buildStartSyncReporterWorker(uniqueSyncId)
                    val upSyncWorkers = upSyncWorkersChain(uniqueSyncId)
                    val downSyncWorkers = downSyncWorkersChain(uniqueSyncId)
                    val chain = upSyncWorkers + downSyncWorkers
                    val endSyncReporterWorker = subjectsSyncSubMasterWorkersBuilder.buildEndSyncReporterWorker(uniqueSyncId)
                    wm.beginWith(startSyncReporterWorker).then(chain).then(endSyncReporterWorker).enqueue()

                    eventSyncCache.clearProgresses()
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

    private suspend fun downSyncWorkersChain(uniqueSyncID: String): List<OneTimeWorkRequest> {
        val downSyncChainRequired = isPeopleDownSyncAllowed()

        return if (downSyncChainRequired) {
            downSyncWorkerBuilder.buildDownSyncWorkerChain(uniqueSyncID)
        } else {
            emptyList()
        }
    }

    private fun isPeopleDownSyncAllowed() = with(preferenceManager) {
        subjectsDownSyncSetting == ON || subjectsDownSyncSetting == EXTRA
    }

    private fun upSyncWorkersChain(uniqueSyncID: String): List<OneTimeWorkRequest> =
        upSyncWorkerBuilder.buildUpSyncWorkerChain(uniqueSyncID)

    private fun clearWorkerHistory(uniqueId: String) {
        val workersRelatedToOtherSync = syncWorkers.filter { !it.isPartOfSubjectsSync(uniqueId) }
        val syncWorkersWithoutSyncId = syncWorkers.filter { it.getUniqueSyncId() == null && it.state != WorkInfo.State.CANCELLED }
        (workersRelatedToOtherSync + syncWorkersWithoutSyncId).forEach {
            wm.cancelWorkById(it.id)
            Timber.tag(SYNC_LOG_TAG).d("Deleted ${it.id} worker")
        }
    }


    private fun getLastSyncId(): String? {
        return syncWorkers.last()?.getUniqueSyncId()
    }

    private fun isSyncRunning(): Boolean = !getWorkInfoForRunningSyncWorkers().isNullOrEmpty()

    private fun getWorkInfoForRunningSyncWorkers(): List<WorkInfo>? {
        return syncWorkers?.filter { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
    }
}
