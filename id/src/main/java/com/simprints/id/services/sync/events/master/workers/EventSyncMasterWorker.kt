package com.simprints.id.services.sync.events.master.workers

import android.content.Context
import androidx.work.*
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.id.services.sync.events.common.getAllSubjectsSyncWorkersInfo
import com.simprints.id.services.sync.events.common.getUniqueSyncId
import com.simprints.id.services.sync.events.common.sortByScheduledTime
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilder
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.EXTRA
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting.ON
import com.simprints.id.services.sync.events.up.EventUpSyncWorkersBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

open class EventSyncMasterWorker(private val appContext: Context,
                                 params: WorkerParameters) : SimCoroutineWorker(appContext, params) {

    companion object {
        const val MIN_BACKOFF_SECS = 15L

        const val MASTER_SYNC_SCHEDULERS = "MASTER_SYNC_SCHEDULERS"
        const val MASTER_SYNC_SCHEDULER_ONE_TIME = "MASTER_SYNC_SCHEDULER_ONE_TIME"
        const val MASTER_SYNC_SCHEDULER_PERIODIC_TIME = "MASTER_SYNC_SCHEDULER_PERIODIC_TIME"

        const val OUTPUT_LAST_SYNC_ID = "OUTPUT_LAST_SYNC_ID"
    }

    override val tag: String = EventSyncMasterWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var downSyncWorkerBuilder: EventDownSyncWorkersBuilder
    @Inject lateinit var upSyncWorkerBuilder: EventUpSyncWorkersBuilder
    @Inject lateinit var preferenceManager: PreferencesManager
    @Inject lateinit var eventSyncCache: EventSyncCache
    @Inject lateinit var eventSyncSubMasterWorkersBuilder: EventSyncSubMasterWorkersBuilder

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
                getComponent<EventSyncMasterWorker> { it.inject(this@EventSyncMasterWorker) }
                crashlyticsLog("Start")

                if (!isSyncRunning()) {
                    val startSyncReporterWorker = eventSyncSubMasterWorkersBuilder.buildStartSyncReporterWorker(uniqueSyncId)
                    val upSyncWorkers = upSyncWorkersChain(uniqueSyncId)
                    val downSyncWorkers = downSyncWorkersChain(uniqueSyncId)
                    val chain = upSyncWorkers + downSyncWorkers
                    val endSyncReporterWorker = eventSyncSubMasterWorkersBuilder.buildEndSyncReporterWorker(uniqueSyncId)
                    wm.beginWith(startSyncReporterWorker).then(chain).then(endSyncReporterWorker).enqueue()

                    eventSyncCache.clearProgresses()

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
        val downSyncChainRequired = isEventDownSyncAllowed()

        return if (downSyncChainRequired) {
            downSyncWorkerBuilder.buildDownSyncWorkerChain(uniqueSyncID)
        } else {
            emptyList()
        }
    }

    private fun isEventDownSyncAllowed() = with(preferenceManager) {
        eventDownSyncSetting == ON || eventDownSyncSetting == EXTRA
    }

    private suspend fun upSyncWorkersChain(uniqueSyncID: String): List<OneTimeWorkRequest> =
        upSyncWorkerBuilder.buildUpSyncWorkerChain(uniqueSyncID)

    private fun getLastSyncId(): String? {
        return syncWorkers.last()?.getUniqueSyncId()
    }

    private fun isSyncRunning(): Boolean = !getWorkInfoForRunningSyncWorkers().isNullOrEmpty()

    private fun getWorkInfoForRunningSyncWorkers(): List<WorkInfo>? {
        return syncWorkers?.filter { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
    }
}
