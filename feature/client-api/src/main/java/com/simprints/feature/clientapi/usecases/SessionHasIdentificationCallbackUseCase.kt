package com.simprints.feature.clientapi.usecases

import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import javax.inject.Inject

internal class SessionHasIdentificationCallbackUseCase @Inject constructor(
    private val eventRepository: EventRepository,
) {
    suspend operator fun invoke(sessionId: String): Boolean = eventRepository
        .getEventsFromScope(sessionId)
        .any { it is IdentificationCallbackEvent }
}
