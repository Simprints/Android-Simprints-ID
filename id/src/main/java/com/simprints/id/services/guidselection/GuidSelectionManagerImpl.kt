package com.simprints.id.services.guidselection

import com.simprints.core.ExternalScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.GuidSelectionEvent
import com.simprints.infra.logging.Simber
import com.simprints.infra.authstore.AuthStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class GuidSelectionManagerImpl @Inject constructor(
    private val authStore: AuthStore,
    private val timerHelper: TimeHelper,
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope
) : GuidSelectionManager {

    override fun handleConfirmIdentityRequest(request: GuidSelectionRequest) {
        try {
            checkRequest(request)
            saveGuidSelectionEvent(request)
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    private fun checkRequest(request: GuidSelectionRequest) {
        if (!authStore.isProjectIdSignedIn(request.projectId)) throw NotSignedInException()
    }

    private fun saveGuidSelectionEvent(request: GuidSelectionRequest) {
        externalScope.launch {
            val event = GuidSelectionEvent(timerHelper.now(), request.selectedGuid)
            eventRepository.addOrUpdateEvent(event)
        }
    }
}
