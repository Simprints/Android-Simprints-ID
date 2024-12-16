package com.simprints.feature.clientapi.usecases

import com.simprints.core.SessionCoroutineScope
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class DeleteSessionEventsIfNeededUseCase @Inject constructor(
    private val configManager: ConfigManager,
    private val eventRepository: EventRepository,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    operator fun invoke(sessionId: String) = sessionCoroutineScope.launch {
        if (!configManager.getProjectConfiguration().canSyncDataToSimprints()) {
            eventRepository.deleteEventScope(sessionId)
        }
    }
}
