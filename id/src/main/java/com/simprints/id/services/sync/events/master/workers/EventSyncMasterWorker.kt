package com.simprints.id.services.sync.events.master.workers

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.settings.canDownSyncEvents
import com.simprints.id.data.prefs.settings.canSyncDataToSimprints
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.id.services.sync.events.common.getAllSubjectsSyncWorkersInfo
import com.simprints.id.services.sync.events.common.getUniqueSyncId
import com.simprints.id.services.sync.events.common.sortByScheduledTime
import com.simprints.id.services.sync.events.down.EventDownSyncWorkersBuilder
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.up.EventUpSyncWorkersBuilder
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

open class EventSyncMasterWorker(
    private val appContext: Context,
    params: WorkerParameters
) : SimCoroutineWorker(appContext, params) {

    companion object {
        const val MIN_BACKOFF_SECS = 15L

        const val MASTER_SYNC_SCHEDULERS = "MASTER_SYNC_SCHEDULERS"
        const val MASTER_SYNC_SCHEDULER_ONE_TIME = "MASTER_SYNC_SCHEDULER_ONE_TIME"
        const val MASTER_SYNC_SCHEDULER_PERIODIC_TIME = "MASTER_SYNC_SCHEDULER_PERIODIC_TIME"

        const val OUTPUT_LAST_SYNC_ID = "OUTPUT_LAST_SYNC_ID"
    }

    override val tag: String = EventSyncMasterWorker::class.java.simpleName

    @Inject
    lateinit var downSyncWorkerBuilder: EventDownSyncWorkersBuilder

    @Inject
    lateinit var upSyncWorkerBuilder: EventUpSyncWorkersBuilder

    @Inject
    lateinit var preferenceManager: IdPreferencesManager

    @Inject
    lateinit var eventSyncCache: EventSyncCache

    @Inject
    lateinit var eventSyncSubMasterWorkersBuilder: EventSyncSubMasterWorkersBuilder

    @Inject
    lateinit var timeHelper: TimeHelper

    @Inject
    lateinit var dispatcher: DispatcherProvider

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

    override suspend fun doWork(): Result {
        getComponent<EventSyncMasterWorker> { it.inject(this@EventSyncMasterWorker) }

        return withContext(dispatcher.io()) {
            try {
                crashlyticsLog("Start")

                if (!preferenceManager.canSyncDataToSimprints() && !preferenceManager.canDownSyncEvents())
                    return@withContext success(
                        message = "Can't sync to SimprintsID, skip"
                    )

                //Requests timestamp now as device is surely ONLINE,
                //so if needed, the NTP has a chance to get refreshed.
                timeHelper.now()

                if (!isSyncRunning()) {
                    val startSyncReporterWorker =
                        eventSyncSubMasterWorkersBuilder.buildStartSyncReporterWorker(uniqueSyncId)
                    val workerChain = mutableListOf<OneTimeWorkRequest>()
                    if (preferenceManager.canSyncDataToSimprints())
                        workerChain += upSyncWorkerBuilder.buildUpSyncWorkerChain(uniqueSyncId).also {
                            Simber.tag(SYNC_LOG_TAG).d("Scheduled ${it.size} up workers")
                        }

                    if (preferenceManager.canDownSyncEvents())
                        workerChain += downSyncWorkerBuilder.buildDownSyncWorkerChain(uniqueSyncId).also {
                            Simber.tag(SYNC_LOG_TAG).d("Scheduled ${it.size} down workers")
                        }

                    val endSyncReporterWorker =
                        eventSyncSubMasterWorkersBuilder.buildEndSyncReporterWorker(uniqueSyncId)
                    wm.beginWith(startSyncReporterWorker).then(workerChain).then(endSyncReporterWorker)
                        .enqueue()

                    eventSyncCache.clearProgresses()

                    success(
                        workDataOf(OUTPUT_LAST_SYNC_ID to uniqueSyncId),
                        "Master work done: new id $uniqueSyncId"
                    )
                } else {
                    val lastSyncId = getLastSyncId()

                    success(
                        workDataOf(OUTPUT_LAST_SYNC_ID to lastSyncId),
                        "Master work done: id already exists $lastSyncId"
                    )
                }
            } catch (t: Throwable) {
                fail(t)
            }
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
