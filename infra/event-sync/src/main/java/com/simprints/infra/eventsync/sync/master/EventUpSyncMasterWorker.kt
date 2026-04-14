package com.simprints.infra.eventsync.sync.master

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.getAllSubjectsSyncWorkersInfo
import com.simprints.infra.eventsync.sync.common.getUniqueSyncId
import com.simprints.infra.eventsync.sync.common.sortByScheduledTime
import com.simprints.infra.eventsync.sync.up.EventUpSyncWorkersBuilder
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.UUID

@HiltWorker
class EventUpSyncMasterWorker @AssistedInject internal constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val upSyncWorkerBuilder: EventUpSyncWorkersBuilder,
    private val configRepository: ConfigRepository,
    private val eventSyncCache: EventSyncCache,
    private val eventRepository: EventRepository,
    private val eventSyncSubMasterWorkersBuilder: EventSyncSubMasterWorkersBuilder,
    private val timeHelper: TimeHelper,
    @param:DispatcherBG private val dispatcher: CoroutineDispatcher,
    private val securityManager: SecurityManager,
) : SimCoroutineWorker(appContext, params) {
    override val tag: String = "EventUpSyncMasterWorker"

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

            if (!configuration.canSyncDataToSimprints()) {
                return@withContext success(message = "Up-sync not allowed by configuration, skip")
            }

            timeHelper.ensureTrustworthiness()

            val upSyncWorkerScopeId = UUID.randomUUID().toString()

            if (!isUpSyncRunning()) {
                val startReporter = eventSyncSubMasterWorkersBuilder.buildStartUpSyncReporterWorker(uniqueSyncId)

                eventRepository.createEventScope(EventScopeType.UP_SYNC, upSyncWorkerScopeId)

                val upSyncWorkers = upSyncWorkerBuilder
                    .buildUpSyncWorkerChain(uniqueSyncId, upSyncWorkerScopeId)
                    .also { Simber.d("Scheduled ${it.size} up workers", tag = tag) }

                val endReporter = eventSyncSubMasterWorkersBuilder.buildEndUpSyncReporterWorker(
                    uniqueSyncId,
                    upSyncWorkerScopeId,
                )

                wm
                    .beginWith(startReporter)
                    .then(upSyncWorkers)
                    .then(endReporter)
                    .enqueue()

                eventSyncCache.clearProgresses()

                success(
                    workDataOf(OUTPUT_LAST_SYNC_ID to uniqueSyncId),
                    "Up-sync master work done: new id $uniqueSyncId",
                )
            } else {
                val lastSyncId = syncWorkers.last().getUniqueSyncId()
                success(
                    workDataOf(OUTPUT_LAST_SYNC_ID to lastSyncId),
                    "Up-sync master work done: id already exists $lastSyncId",
                )
            }
        } catch (t: Throwable) {
            fail(t)
        }
    }

    private fun isUpSyncRunning(): Boolean = syncWorkers
        .filter { it.state == androidx.work.WorkInfo.State.RUNNING || it.state == androidx.work.WorkInfo.State.ENQUEUED }
        .isNotEmpty()

    companion object {
        const val OUTPUT_LAST_SYNC_ID = "OUTPUT_LAST_UP_SYNC_ID"
    }
}
