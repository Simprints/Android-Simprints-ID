package com.simprints.infra.eventsync.sync.master

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.getAllSubjectsSyncWorkersInfo
import com.simprints.infra.eventsync.sync.common.getUniqueSyncId
import com.simprints.infra.eventsync.sync.common.sortByScheduledTime
import com.simprints.infra.eventsync.sync.down.CommCareEventSyncWorkersBuilder
import com.simprints.infra.eventsync.sync.down.SimprintsEventDownSyncWorkersBuilder
import com.simprints.infra.eventsync.sync.up.EventUpSyncWorkersBuilder
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.UUID

@HiltWorker
class EventSyncMasterWorker @AssistedInject internal constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val simprintsDownSyncWorkerBuilder: SimprintsEventDownSyncWorkersBuilder,
    private val commCareDownSyncWorkerBuilder: CommCareEventSyncWorkersBuilder,
    private val upSyncWorkerBuilder: EventUpSyncWorkersBuilder,
    private val configManager: ConfigManager,
    private val eventSyncCache: EventSyncCache,
    private val eventRepository: EventRepository,
    private val eventSyncSubMasterWorkersBuilder: EventSyncSubMasterWorkersBuilder,
    private val timeHelper: TimeHelper,
    @param:DispatcherBG private val dispatcher: CoroutineDispatcher,
    private val securityManager: SecurityManager,
) : SimCoroutineWorker(appContext, params) {
    override val tag: String = "EventSyncMasterWorker"

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

    override suspend fun doWork(): Result = withContext(dispatcher) {
        showProgressNotification()
        crashlyticsLog("Started")
        try {
            // check if device is rooted before starting the sync
            securityManager.checkIfDeviceIsRooted()
            val configuration = configManager.getProjectConfiguration()

            if (!configuration.canSyncDataToSimprints() && !isEventDownSyncAllowed(configuration)) {
                return@withContext success(message = "Can't sync to SimprintsID, skip")
            }

            // Requests NTP sync now as device is surely ONLINE,
            timeHelper.ensureTrustworthiness()

            val downSyncWorkerScopeId = UUID.randomUUID().toString()
            val upSyncWorkerScopeId = UUID.randomUUID().toString()

            if (!isSyncRunning()) {
                val startSyncReporterWorker =
                    eventSyncSubMasterWorkersBuilder.buildStartSyncReporterWorker(uniqueSyncId)
                val workerChain = mutableListOf<OneTimeWorkRequest>()
                if (configuration.canSyncDataToSimprints()) {
                    eventRepository.createEventScope(
                        EventScopeType.UP_SYNC,
                        upSyncWorkerScopeId,
                    )

                    workerChain += upSyncWorkerBuilder
                        .buildUpSyncWorkerChain(
                            uniqueSyncId,
                            upSyncWorkerScopeId,
                        ).also { Simber.d("Scheduled ${it.size} up workers", tag = tag) }
                }

                val isDownSyncAllowedInWorker = inputData.getBoolean(IS_DOWN_SYNC_ALLOWED, true)
                if (configuration.isSimprintsEventDownSyncAllowed() && isDownSyncAllowedInWorker) {
                    // TODO: Remove after all users have updated to 2025.3.0
                    // In versions before 2025.3.0 a bug prevented single subject down-sync scopes from being closed and uploaded.
                    // Attempting to close any such scopes and recover at least some of the data.
                    eventRepository.closeAllOpenScopes(EventScopeType.DOWN_SYNC, null)

                    eventRepository.createEventScope(
                        EventScopeType.DOWN_SYNC,
                        downSyncWorkerScopeId,
                    )

                    workerChain += simprintsDownSyncWorkerBuilder
                        .buildDownSyncWorkerChain(
                            uniqueSyncId,
                            downSyncWorkerScopeId,
                        ).also { Simber.d("Scheduled ${it.size} Simprints down workers", tag = tag) }
                } else if (configuration.isCommCareEventDownSyncAllowed()) {
                    eventRepository.createEventScope(
                        EventScopeType.DOWN_SYNC,
                        downSyncWorkerScopeId,
                    )

                    workerChain += commCareDownSyncWorkerBuilder
                        .buildDownSyncWorkerChain(
                            uniqueSyncId,
                            downSyncWorkerScopeId,
                        ).also { Simber.d("Scheduled ${it.size} CommCare down workers", tag = tag) }
                }

                val endSyncReporterWorker =
                    eventSyncSubMasterWorkersBuilder.buildEndSyncReporterWorker(
                        uniqueSyncId,
                        downSyncWorkerScopeId,
                        upSyncWorkerScopeId,
                    )

                wm
                    .beginWith(startSyncReporterWorker)
                    .then(workerChain)
                    .then(endSyncReporterWorker)
                    .enqueue()

                eventSyncCache.clearProgresses()

                success(
                    workDataOf(OUTPUT_LAST_SYNC_ID to uniqueSyncId),
                    "Master work done: new id $uniqueSyncId",
                )
            } else {
                val lastSyncId = getLastSyncId()

                success(
                    workDataOf(OUTPUT_LAST_SYNC_ID to lastSyncId),
                    "Master work done: id already exists $lastSyncId",
                )
            }
        } catch (t: Throwable) {
            fail(t)
        }
    }

    private suspend fun isEventDownSyncAllowed(configuration: ProjectConfiguration): Boolean {
        val isProjectPaused = configManager.getProject()?.state == ProjectState.PROJECT_PAUSED
        val isSimprintsDownSyncEnabled = configuration.isSimprintsEventDownSyncAllowed()
        val isCommCareDownSyncEnabled = configuration.isCommCareEventDownSyncAllowed()

        return !isProjectPaused && (isSimprintsDownSyncEnabled || isCommCareDownSyncEnabled)
    }

    private fun getLastSyncId(): String? = syncWorkers.last().getUniqueSyncId()

    private fun isSyncRunning(): Boolean = getWorkInfoForRunningSyncWorkers().isNotEmpty()

    private fun getWorkInfoForRunningSyncWorkers(): List<WorkInfo> = syncWorkers.filter {
        it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
    }

    companion object {
        const val OUTPUT_LAST_SYNC_ID = "OUTPUT_LAST_SYNC_ID"
        const val IS_DOWN_SYNC_ALLOWED = "IS_DOWN_SYNC_ALLOWED"
    }
}
