package com.simprints.infra.events.receivers

import com.simprints.core.SessionCoroutineScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.session.SessionEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class CloseSessionIfPresentUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    operator fun invoke() = sessionCoroutineScope.launch {
        if (eventRepository.hasOpenSession()) {
            eventRepository.closeCurrentSession(EventScopeEndCause.WORKFLOW_ENDED)
        }
    }
}
