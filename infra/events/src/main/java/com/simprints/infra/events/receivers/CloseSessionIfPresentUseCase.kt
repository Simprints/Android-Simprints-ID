package com.simprints.infra.events.receivers

import com.simprints.core.ExternalScope
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.ArtificialTerminationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class CloseSessionIfPresentUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope,
) {

    operator fun invoke() = externalScope.launch {
        if (eventRepository.hasOpenSession()) {
            eventRepository.closeCurrentSession(ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.TIMED_OUT)
        }
    }
}
