package com.simprints.feature.clientapi.usecases

import com.simprints.core.ExternalScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.canSyncDataToSimprints
import com.simprints.infra.events.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class DeleteSessionEventsIfNeededUseCase @Inject constructor(
    private val configManager: ConfigManager,
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope
) {

    suspend operator fun invoke(sessionId: String) = externalScope.launch {
        if (!configManager.getProjectConfiguration().canSyncDataToSimprints()) {
            eventRepository.deleteSessionEvents(sessionId)
        }
    }
}
