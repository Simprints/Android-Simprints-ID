package com.simprints.infra.events.receivers

import com.simprints.core.ExternalScope
import com.simprints.infra.events.SessionEventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class CloseSessionIfPresentUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    @ExternalScope private val externalScope: CoroutineScope,
) {

    operator fun invoke() = externalScope.launch {
        if (eventRepository.hasOpenSession()) {
            eventRepository.closeCurrentSession(EventScopeEndCause.WORKFLOW_ENDED)
        }
    }
}
