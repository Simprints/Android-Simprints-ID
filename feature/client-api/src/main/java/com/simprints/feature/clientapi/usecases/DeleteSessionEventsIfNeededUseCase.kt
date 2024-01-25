package com.simprints.feature.clientapi.usecases

import com.simprints.core.ExternalScope
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.events.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class DeleteSessionEventsIfNeededUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope,
) {

    suspend operator fun invoke(sessionId: String) = externalScope.launch {
        if (!configRepository.getProjectConfiguration().canSyncDataToSimprints()) {
            eventRepository.deleteSession(sessionId)
        }
    }
}
