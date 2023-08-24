package com.simprints.feature.clientapi.session

import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent
import kotlinx.coroutines.flow.toList
import javax.inject.Inject

internal class ClientSessionManager @Inject constructor(
    private val coreEventRepository: EventRepository,
) {

    suspend fun getCurrentSessionId(): String = coreEventRepository.getCurrentCaptureSessionEvent().id

    suspend fun isCurrentSessionAnIdentificationOrEnrolment(): Boolean {
        val session = coreEventRepository.getCurrentCaptureSessionEvent()
        return coreEventRepository.observeEventsFromSession(session.id).toList().any {
            it is IdentificationCalloutEvent || it is EnrolmentCalloutEvent
        }
    }

    suspend fun sessionHasIdentificationCallback(sessionId: String): Boolean = coreEventRepository
        .observeEventsFromSession(sessionId)
        .toList()
        .any { it is IdentificationCallbackEvent }
}
