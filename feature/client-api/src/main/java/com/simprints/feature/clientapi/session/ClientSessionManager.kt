package com.simprints.feature.clientapi.session

import com.simprints.core.ExternalScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class ClientSessionManager @Inject constructor(
    private val coreEventRepository: EventRepository,
    private val timeHelper: TimeHelper,
    @ExternalScope private val externalScope: CoroutineScope
) {

    suspend fun getCurrentSessionId(): String = coreEventRepository.getCurrentCaptureSessionEvent().id

    suspend fun isCurrentSessionAnIdentificationOrEnrolment(): Boolean = getCurrentSessionId()
        .let { coreEventRepository.observeEventsFromSession(it).toList() }
        .any { it is IdentificationCalloutEvent || it is EnrolmentCalloutEvent }

    suspend fun sessionHasIdentificationCallback(sessionId: String): Boolean = coreEventRepository
        .observeEventsFromSession(sessionId)
        .toList()
        .any { it is IdentificationCallbackEvent }

    fun reportUnknownExtras(unknownExtras: Map<String, Any?>) {
        if (unknownExtras.isNotEmpty()) {
            externalScope.launch {
                coreEventRepository.addOrUpdateEvent(SuspiciousIntentEvent(timeHelper.now(), unknownExtras))
            }
        }
    }

    fun addInvalidIntentEvent(action: String, extras: Map<String, Any>) {
        externalScope.launch {
            coreEventRepository.addOrUpdateEvent(InvalidIntentEvent(timeHelper.now(), action, extras))
        }
    }

    fun addCompletionCheckEvent(flowCompleted: Boolean) {
        externalScope.launch {
            coreEventRepository.addOrUpdateEvent(CompletionCheckEvent(timeHelper.now(), flowCompleted))
        }
    }

    suspend fun closeCurrentSessionNormally() {
        coreEventRepository.closeCurrentSession()
    }

    fun deleteSessionEvents(sessionId: String) {
        externalScope.launch {
            coreEventRepository.deleteSessionEvents(sessionId)
        }
    }
}
