package com.simprints.infra.eventsync.sync.master

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.getAllSubjectsSyncWorkersInfo
import com.simprints.infra.eventsync.sync.common.getUniqueSyncId
import com.simprints.infra.eventsync.sync.common.sortByScheduledTime
import com.simprints.infra.eventsync.sync.down.CommCareEventSyncWorkersBuilder
import com.simprints.infra.eventsync.sync.down.SimprintsEventDownSyncWorkersBuilder
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.UUID

@HiltWorker
class EventDownSyncMasterWorker @AssistedInject internal constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val simprintsDownSyncWorkerBuilder: SimprintsEventDownSyncWorkersBuilder,
    private val commCareDownSyncWorkerBuilder: CommCareEventSyncWorkersBuilder,
    private val configRepository: ConfigRepository,
    private val eventSyncCache: EventSyncCache,
    private val eventRepository: EventRepository,
    private val eventSyncSubMasterWorkersBuilder: EventSyncSubMasterWorkersBuilder,
    private val timeHelper: TimeHelper,
    @param:DispatcherBG private val dispatcher: CoroutineDispatcher,
    private val securityManager: SecurityManager,
) : SimCoroutineWorker(appContext, params) {
    override val tag: String = "EventDownSyncMasterWorker"

    private val wm = WorkManager.getInstance(appContext)

    private val syncWorkers
        get() = wm.getAllSubjectsSyncWorkersInfo().get().apply {
            if (this.isNullOrEmpty()) {
                this.sortByScheduledTime()
            }
        }

    val uniqueSyncId by lazy { UUID.randomUUID().toString() }

    override suspend fun doWork() = withContext(dispatcher) {
        showProgressNotification()
        crashlyticsLog("Started")
        try {
            securityManager.checkIfDeviceIsRooted()
            val configuration = configRepository.getProjectConfiguration()

            val isProjectPaused = configRepository.getProject()?.state == ProjectState.PROJECT_PAUSED
            val isSimprintsDownSyncEnabled = configuration.isSimprintsEventDownSyncAllowed()
            val isCommCareDownSyncEnabled = configuration.isCommCareEventDownSyncAllowed()

            if (isProjectPaused || (!isSimprintsDownSyncEnabled && !isCommCareDownSyncEnabled)) {
                return@withContext success(message = "Down-sync not allowed, skip")
            }

            timeHelper.ensureTrustworthiness()

            val downSyncWorkerScopeId = UUID.randomUUID().toString()

            if (!isDownSyncRunning()) {
                val startReporter = eventSyncSubMasterWorkersBuilder.buildStartDownSyncReporterWorker(uniqueSyncId)

                // TODO: Remove after all users have updated to 2025.3.0
                eventRepository.closeAllOpenScopes(EventScopeType.DOWN_SYNC, null)
                eventRepository.createEventScope(EventScopeType.DOWN_SYNC, downSyncWorkerScopeId)

                val downSyncWorkers = if (isSimprintsDownSyncEnabled) {
                    simprintsDownSyncWorkerBuilder
                        .buildDownSyncWorkerChain(uniqueSyncId, downSyncWorkerScopeId)
                        .also { Simber.d("Scheduled ${it.size} Simprints down workers", tag = tag) }
                } else {
                    commCareDownSyncWorkerBuilder
                        .buildDownSyncWorkerChain(uniqueSyncId, downSyncWorkerScopeId)
                        .also { Simber.d("Scheduled ${it.size} CommCare down workers", tag = tag) }
                }

                val endReporter = eventSyncSubMasterWorkersBuilder.buildEndDownSyncReporterWorker(
                    uniqueSyncId,
                    downSyncWorkerScopeId,
                )

                wm
                    .beginWith(startReporter)
                    .then(downSyncWorkers)
                    .then(endReporter)
                    .enqueue()

                eventSyncCache.clearProgresses()

                success(
                    workDataOf(OUTPUT_LAST_SYNC_ID to uniqueSyncId),
                    "Down-sync master work done: new id $uniqueSyncId",
                )
            } else {
                val lastSyncId = syncWorkers.last().getUniqueSyncId()
                success(
                    workDataOf(OUTPUT_LAST_SYNC_ID to lastSyncId),
                    "Down-sync master work done: id already exists $lastSyncId",
                )
            }
        } catch (t: Throwable) {
            fail(t)
        }
    }

    private fun isDownSyncRunning(): Boolean = syncWorkers
        .filter { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
        .isNotEmpty()

    companion object {
        const val OUTPUT_LAST_SYNC_ID = "OUTPUT_LAST_DOWN_SYNC_ID"
    }
}
