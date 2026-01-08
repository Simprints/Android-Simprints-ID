package com.simprints.feature.clientapi.usecases

import com.simprints.core.SessionCoroutineScope
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.events.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class DeleteSessionEventsIfNeededUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val eventRepository: EventRepository,
    @param:SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    operator fun invoke(sessionId: String) = sessionCoroutineScope.launch {
        val projectNotRunning = configRepository.getProject()?.state != ProjectState.RUNNING
        val simprintsDataSyncDisabled = !configRepository.getProjectConfiguration().canSyncDataToSimprints()

        if (simprintsDataSyncDisabled || projectNotRunning) {
            eventRepository.deleteEventScope(sessionId)
        }
    }
}
