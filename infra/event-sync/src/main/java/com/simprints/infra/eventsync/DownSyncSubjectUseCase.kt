package com.simprints.infra.eventsync

import com.simprints.core.DispatcherIO
import com.simprints.core.tools.utils.ExtractCommCareCaseIdUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.RemoteEventQuery
import com.simprints.infra.eventsync.sync.down.tasks.CommCareEventSyncTask
import com.simprints.infra.eventsync.sync.down.tasks.SimprintsEventDownSyncTask
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DownSyncSubjectUseCase @Inject internal constructor(
    private val eventRepository: EventRepository,
    private val simprintsDownSyncTask: SimprintsEventDownSyncTask,
    private val commCareSyncTask: CommCareEventSyncTask,
    private val configRepository: ConfigRepository,
    private val extractCommCareCaseId: ExtractCommCareCaseIdUseCase,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        projectId: String,
        subjectId: String,
        metadata: String,
    ): Unit = withContext(dispatcher) {
        val project = configRepository.getProject() ?: return@withContext
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
            simprintsDownSyncTask.downSync(this, op, eventScope, project).toList()
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
            commCareSyncTask.downSync(this, op, eventScope, project).toList()
        }
        eventRepository.closeEventScope(eventScope, EventScopeEndCause.WORKFLOW_ENDED)
    }

    private fun getProjectModalities(projectConfiguration: ProjectConfiguration) = projectConfiguration.general.modalities
    
}
