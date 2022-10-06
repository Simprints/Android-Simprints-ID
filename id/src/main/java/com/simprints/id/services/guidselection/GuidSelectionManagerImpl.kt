package com.simprints.id.services.guidselection

import com.simprints.core.tools.extentions.inBackground
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.GuidSelectionEvent
import com.simprints.id.exceptions.safe.secure.NotSignedInException
import com.simprints.id.orchestrator.steps.core.requests.GuidSelectionRequest
import com.simprints.core.tools.exceptions.ignoreException
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager

class GuidSelectionManagerImpl(
    val deviceId: String,
    val loginManager: LoginManager,
    private val timerHelper: TimeHelper,
    val eventRepository: EventRepository
) : GuidSelectionManager {

    override suspend fun handleConfirmIdentityRequest(request: GuidSelectionRequest) {
        try {
            checkRequest(request)
            saveGuidSelectionEvent(request)
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    private fun checkRequest(request: GuidSelectionRequest) {
        if (!loginManager.isProjectIdSignedIn(request.projectId)) throw NotSignedInException()
    }

    private suspend fun saveGuidSelectionEvent(request: GuidSelectionRequest) =
        ignoreException {
            val event = GuidSelectionEvent(timerHelper.now(), request.selectedGuid)
            inBackground { eventRepository.addOrUpdateEvent(event) }
        }
}
