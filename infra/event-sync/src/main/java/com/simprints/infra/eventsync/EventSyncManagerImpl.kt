package com.simprints.infra.eventsync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.tokenization.values
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEventType
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.RemoteEventQuery
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.sync.EventSyncStateProcessor
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.MASTER_SYNC_SCHEDULERS
import com.simprints.infra.eventsync.sync.common.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.infra.eventsync.sync.common.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.infra.eventsync.sync.common.SYNC_LOG_TAG
import com.simprints.infra.eventsync.sync.common.addTagForBackgroundSyncMasterWorker
import com.simprints.infra.eventsync.sync.common.addTagForOneTimeSyncMasterWorker
import com.simprints.infra.eventsync.sync.common.addTagForScheduledAtNow
import com.simprints.infra.eventsync.sync.common.addTagForSyncMasterWorkers
import com.simprints.infra.eventsync.sync.common.cancelAllSubjectsSyncWorkers
import com.simprints.infra.eventsync.sync.down.tasks.EventDownSyncTask
import com.simprints.infra.eventsync.sync.master.EventSyncMasterWorker
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class EventSyncManagerImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val eventSyncStateProcessor: EventSyncStateProcessor,
    private val downSyncScopeRepository: EventDownSyncScopeRepository,
    private val eventRepository: EventRepository,
    private val upSyncScopeRepo: EventUpSyncScopeRepository,
    private val eventSyncCache: EventSyncCache,
    private val downSyncTask: EventDownSyncTask,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val configRepository: ConfigRepository,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : EventSyncManager {

    companion object {
        private const val SYNC_REPEAT_INTERVAL = BuildConfig.SYNC_PERIODIC_WORKER_INTERVAL_MINUTES
        val SYNC_REPEAT_UNIT = TimeUnit.MINUTES
    }

    private val wm = WorkManager.getInstance(ctx)

    override suspend fun getLastSyncTime(): Date? = eventSyncCache.readLastSuccessfulSyncTime()

    override fun getLastSyncState(): LiveData<EventSyncState> =
        eventSyncStateProcessor.getLastSyncState()

    override fun sync() {
        Simber.tag(SYNC_LOG_TAG).d("[SCHEDULER] One time events master worker")

        wm.beginUniqueWork(
            MASTER_SYNC_SCHEDULER_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            buildOneTimeRequest()
        ).enqueue()
    }

    override fun scheduleSync() {
        Simber.tag(SYNC_LOG_TAG).d("[SCHEDULER] Periodic events master worker")

        wm.enqueueUniquePeriodicWork(
            MASTER_SYNC_SCHEDULER_PERIODIC_TIME,
            ExistingPeriodicWorkPolicy.UPDATE,
            buildPeriodicRequest()
        )
    }

    private fun buildOneTimeRequest(): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventSyncMasterWorker::class.java)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTagForSyncMasterWorkers()
            .addTagForOneTimeSyncMasterWorker()
            .addTagForScheduledAtNow()
            .build() as OneTimeWorkRequest

    private fun buildPeriodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequest.Builder(
            EventSyncMasterWorker::class.java,
            SYNC_REPEAT_INTERVAL,
            SYNC_REPEAT_UNIT
        )
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTagForSyncMasterWorkers()
            .addTagForBackgroundSyncMasterWorker()
            .addTagForScheduledAtNow()
            .build() as PeriodicWorkRequest

    private fun getDownSyncMasterWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    override fun cancelScheduledSync() {
        wm.cancelAllWorkByTag(MASTER_SYNC_SCHEDULERS)
        stop()
    }

    override fun stop() {
        wm.cancelAllSubjectsSyncWorkers()
    }

    private fun cleanScheduledHistory() {
        wm.pruneWork()
    }

    override suspend fun countEventsToUpload(type: EventType?): Flow<Int> =
        eventRepository.observeEventCount(type)

    override suspend fun countEventsToDownload(): DownSyncCounts {
        val projectConfig = configRepository.getProjectConfiguration()
        val deviceConfig = configRepository.getDeviceConfiguration()

        val downSyncScope = downSyncScopeRepository.getDownSyncScope(
            modes = getProjectModes(projectConfig),
            selectedModuleIDs = deviceConfig.selectedModules.values(),
            syncPartitioning = projectConfig.synchronization.down.partitionType.toDomain()
        )

        var creationsToDownload = 0
        var deletionsToDownload = 0

        downSyncScope.operations.forEach { syncOperation ->
            val counts = eventRemoteDataSource.count(syncOperation.queryEvent.fromDomainToApi())

            creationsToDownload += counts
                .firstOrNull { it.type == EnrolmentRecordEventType.EnrolmentRecordCreation }
                ?.count ?: 0
            deletionsToDownload += counts
                .firstOrNull { it.type == EnrolmentRecordEventType.EnrolmentRecordDeletion }
                ?.count ?: 0
        }

        return DownSyncCounts(creationsToDownload, deletionsToDownload)
    }

    override suspend fun downSyncSubject(
        projectId: String,
        subjectId: String,
    ): Unit = withContext(dispatcher) {
        val eventScope = eventRepository.createEventScope(EventScopeType.DOWN_SYNC)
        val op = EventDownSyncOperation(RemoteEventQuery(
            projectId = projectId,
            subjectId = subjectId,
            modes = getProjectModes(configRepository.getProjectConfiguration()),
        ))
        downSyncTask.downSync(this, op, eventScope).toList()
    }

    private fun getProjectModes(projectConfiguration: ProjectConfiguration) =
        projectConfiguration.general.modalities.map { it.toMode() }

    override suspend fun deleteModules(unselectedModules: List<String>) {
        downSyncScopeRepository.deleteOperations(
            unselectedModules,
            modes = getProjectModes(configRepository.getProjectConfiguration()),
        )
    }

    override suspend fun deleteSyncInfo() {
        downSyncScopeRepository.deleteAll()
        upSyncScopeRepo.deleteAll()
        eventSyncCache.clearProgresses()
        eventSyncCache.storeLastSuccessfulSyncTime(null)
        cleanScheduledHistory()
    }

    override suspend fun resetDownSyncInfo() {
        downSyncScopeRepository.deleteAll()
    }
}
