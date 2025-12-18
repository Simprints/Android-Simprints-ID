package com.simprints.infra.eventsync

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.tokenization.values
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.ExtractCommCareCaseIdUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.eventsync.event.commcare.cache.CommCareSyncCache
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
import com.simprints.infra.eventsync.sync.down.tasks.CommCareEventSyncTask
import com.simprints.infra.eventsync.sync.down.tasks.SimprintsEventDownSyncTask
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    private val commCareSyncCache: CommCareSyncCache,
    private val simprintsDownSyncTask: SimprintsEventDownSyncTask,
    private val commCareSyncTask: CommCareEventSyncTask,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val configRepository: ConfigRepository,
    private val extractCommCareCaseId: ExtractCommCareCaseIdUseCase,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher,
) : EventSyncManager {
    override suspend fun getLastSyncTime(): Timestamp? = eventSyncCache.readLastSuccessfulSyncTime()

    override fun getLastSyncState(useDefaultValue: Boolean): LiveData<EventSyncState> = MediatorLiveData<EventSyncState>().apply {
        if (useDefaultValue) {
            value = EventSyncState(syncId = "", null, null, emptyList(), emptyList(), emptyList())
        }
        addSource(eventSyncStateProcessor.getLastSyncState()) { lastSyncState ->
            value = lastSyncState
        }
    }

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

    override suspend fun countEventsToUpload(): Flow<Int> = eventRepository.observeEventCount(null)

    override suspend fun countEventsToUpload(types: List<EventType>): Flow<Int> = combine(
        types.map { eventRepository.observeEventCount(it) },
    ) { it.sum() }

    override suspend fun countEventsToDownload(): DownSyncCounts {
        val projectConfig = configRepository.getProjectConfiguration()
        val simprintsDownConfig = projectConfig.synchronization.down.simprints
        // For CommCare there's no easy way to count the number of events to download
        if (simprintsDownConfig == null) {
            return DownSyncCounts(count = 0, isLowerBound = false)
        }
        val deviceConfig = configRepository.getDeviceConfiguration()

        val downSyncScope = downSyncScopeRepository.getDownSyncScope(
            modes = getProjectModalities(projectConfig),
            selectedModuleIDs = deviceConfig.selectedModules.values(),
            syncPartitioning = simprintsDownConfig.partitionType.toDomain(),
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
        metadata: String,
    ): Unit = withContext(dispatcher) {
        val projectConfiguration = configRepository.getProjectConfiguration()

        val eventScope = eventRepository.createEventScope(EventScopeType.DOWN_SYNC)
        if (projectConfiguration.synchronization.down.simprints != null) {
            val op = EventDownSyncOperation(
                RemoteEventQuery(
                    projectId = projectId,
                    subjectId = subjectId,
                    modes = getProjectModalities(projectConfiguration),
                ),
            )
            simprintsDownSyncTask.downSync(this, op, eventScope, configRepository.getProject()).toList()
        } else if (projectConfiguration.synchronization.down.commCare != null) {
            val caseId = extractCommCareCaseId(metadata)
            val op = EventDownSyncOperation(
                RemoteEventQuery(
                    projectId = projectId,
                    subjectId = subjectId,
                    externalIds = caseId?.let { listOf(it) },
                    modes = getProjectModalities(projectConfiguration),
                ),
            )
            commCareSyncTask.downSync(this, op, eventScope, configRepository.getProject()).toList()
        }
        eventRepository.closeEventScope(eventScope, EventScopeEndCause.WORKFLOW_ENDED)
    }

    private fun getProjectModalities(projectConfiguration: ProjectConfiguration) = projectConfiguration.general.modalities

    override suspend fun deleteModules(unselectedModules: List<String>) {
        downSyncScopeRepository.deleteOperations(
            unselectedModules,
            modes = getProjectModalities(configRepository.getProjectConfiguration()),
        )
    }

    override suspend fun deleteSyncInfo() {
        downSyncScopeRepository.deleteAll()
        commCareSyncCache.clearAllSyncedCases()
        upSyncScopeRepo.deleteAll()
        eventSyncCache.clearProgresses()
        eventSyncCache.storeLastSuccessfulSyncTime(null)
    }

    override suspend fun resetDownSyncInfo() {
        downSyncScopeRepository.deleteAll()
        commCareSyncCache.clearAllSyncedCases()
    }
}
