package com.simprints.infra.eventsync.sync.master

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.store.models.isEventDownSyncAllowed
import com.simprints.infra.eventsync.sync.common.*
import com.simprints.infra.eventsync.sync.down.EventDownSyncWorkersBuilder
import com.simprints.infra.eventsync.sync.up.EventUpSyncWorkersBuilder
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.*

@HiltWorker
internal class EventSyncMasterWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val downSyncWorkerBuilder: EventDownSyncWorkersBuilder,
    private val upSyncWorkerBuilder: EventUpSyncWorkersBuilder,
    private val configRepository: ConfigRepository,
    private val eventSyncCache: EventSyncCache,
    private val eventSyncSubMasterWorkersBuilder: EventSyncSubMasterWorkersBuilder,
    private val timeHelper: TimeHelper,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
    private val securityManager: SecurityManager,
) : SimCoroutineWorker(appContext, params) {

    companion object {

        const val OUTPUT_LAST_SYNC_ID = "OUTPUT_LAST_SYNC_ID"
    }

    override val tag: String = EventSyncMasterWorker::class.java.simpleName

    private val wm = WorkManager.getInstance(appContext)

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
        withContext(dispatcher) {
            try {
                // check if device is rooted before starting the sync
                securityManager.checkIfDeviceIsRooted()
                crashlyticsLog("Start")
                showProgressNotification()
                val configuration = configRepository.getProjectConfiguration()

                if (!configuration.canSyncDataToSimprints() && !isEventDownSyncAllowed(configuration)) return@withContext success(
                    message = "Can't sync to SimprintsID, skip"
                )

                // Requests NTP sync now as device is surely ONLINE,
                timeHelper.ensureTrustworthiness()

                if (!isSyncRunning()) {
                    val startSyncReporterWorker =
                        eventSyncSubMasterWorkersBuilder.buildStartSyncReporterWorker(uniqueSyncId)
                    val workerChain = mutableListOf<OneTimeWorkRequest>()
                    if (configuration.canSyncDataToSimprints())
                        workerChain += upSyncWorkerBuilder.buildUpSyncWorkerChain(uniqueSyncId)
                            .also {
                                Simber.tag(SYNC_LOG_TAG).d("Scheduled ${it.size} up workers")
                            }

                    if (configuration.isEventDownSyncAllowed())
                        workerChain += downSyncWorkerBuilder.buildDownSyncWorkerChain(uniqueSyncId)
                            .also {
                                Simber.tag(SYNC_LOG_TAG).d("Scheduled ${it.size} down workers")
                            }

                    val endSyncReporterWorker =
                        eventSyncSubMasterWorkersBuilder.buildEndSyncReporterWorker(uniqueSyncId)
                    wm.beginWith(startSyncReporterWorker).then(workerChain)
                        .then(endSyncReporterWorker)
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

    private suspend fun isEventDownSyncAllowed(configuration: ProjectConfiguration): Boolean {
        val isProjectPaused =
            configRepository.getProject(configuration.projectId).state == ProjectState.PROJECT_PAUSED

        val isDownSyncConfigEnabled =
            configuration.synchronization.frequency != SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC

        return !isProjectPaused && isDownSyncConfigEnabled
    }

    private fun getLastSyncId(): String? {
        return syncWorkers.last()?.getUniqueSyncId()
    }

    private fun isSyncRunning(): Boolean = !getWorkInfoForRunningSyncWorkers().isNullOrEmpty()

    private fun getWorkInfoForRunningSyncWorkers(): List<WorkInfo>? {
        return syncWorkers?.filter { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
    }
}
