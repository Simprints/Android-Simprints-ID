package com.simprints.infra.eventsync

import androidx.lifecycle.LiveData
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.tokenization.values
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
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
import com.simprints.infra.eventsync.sync.common.TAG_SCHEDULED_AT
import com.simprints.infra.eventsync.sync.common.TAG_SUBJECTS_SYNC_ALL_WORKERS
import com.simprints.infra.eventsync.sync.down.tasks.EventDownSyncTask
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class EventSyncManagerImpl @Inject constructor(
    private val timeHelper: TimeHelper,
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
    override suspend fun getLastSyncTime(): Timestamp? = eventSyncCache.readLastSuccessfulSyncTime()

    override fun getLastSyncState(): LiveData<EventSyncState> = eventSyncStateProcessor.getLastSyncState()

    override fun getPeriodicWorkTags(): List<String> = listOf(
        MASTER_SYNC_SCHEDULERS,
        MASTER_SYNC_SCHEDULER_PERIODIC_TIME,
        "$TAG_SCHEDULED_AT${timeHelper.now().ms}",
    )

    override fun getOneTimeWorkTags(): List<String> = listOf(
        MASTER_SYNC_SCHEDULERS,
        MASTER_SYNC_SCHEDULER_ONE_TIME,
        "$TAG_SCHEDULED_AT${timeHelper.now().ms}",
    )

    override fun getAllWorkerTag(): String = TAG_SUBJECTS_SYNC_ALL_WORKERS

    override suspend fun countEventsToUpload(type: EventType?): Flow<Int> = eventRepository.observeEventCount(type)

    override suspend fun countEventsToDownload(): DownSyncCounts {
        val projectConfig = configRepository.getProjectConfiguration()
        val deviceConfig = configRepository.getDeviceConfiguration()

        val downSyncScope = downSyncScopeRepository.getDownSyncScope(
            modes = getProjectModes(projectConfig),
            selectedModuleIDs = deviceConfig.selectedModules.values(),
            syncPartitioning = projectConfig.synchronization.down.partitionType
                .toDomain(),
        )

        val counts = downSyncScope.operations
            .map { eventRemoteDataSource.count(it.queryEvent.fromDomainToApi()) }

        return DownSyncCounts(
            count = counts.sumOf { it.count },
            isLowerBound = counts.any { it.isLowerBound },
        )
    }

    override suspend fun downSyncSubject(
        projectId: String,
        subjectId: String,
    ): Unit = withContext(dispatcher) {
        val eventScope = eventRepository.createEventScope(EventScopeType.DOWN_SYNC)
        val op = EventDownSyncOperation(
            RemoteEventQuery(
                projectId = projectId,
                subjectId = subjectId,
                modes = getProjectModes(configRepository.getProjectConfiguration()),
            ),
        )
        downSyncTask.downSync(this, op, eventScope, configRepository.getProject()).toList()
    }

    private fun getProjectModes(projectConfiguration: ProjectConfiguration) = projectConfiguration.general.modalities.map { it.toMode() }

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
    }

    override suspend fun resetDownSyncInfo() {
        downSyncScopeRepository.deleteAll()
    }
}
