package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.controllers.core.eventData.model.fromDomainToCore
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.core.tools.extentions.inBackground
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.*
import com.simprints.id.data.db.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.id.data.db.event.domain.models.callout.IdentificationCalloutEvent
import com.simprints.libsimprints.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType as CoreAlertScreenEventType

class ClientApiSessionEventsManagerImpl(private val coreEventRepository: EventRepository,
                                        private val timeHelper: ClientApiTimeHelper,
                                        private val dispatcher: CoroutineDispatcher = Dispatchers.IO) :
    ClientApiSessionEventsManager {

    override suspend fun createSession(integration: IntegrationInfo): String {
        runBlocking {
            coreEventRepository.createSession(BuildConfig.VERSION_NAME)
        }

        inBackground(dispatcher) { coreEventRepository.addEventToCurrentSession(IntentParsingEvent(timeHelper.now(), integration.fromDomainToCore())) }

        return getCurrentSessionId()
    }

    override suspend fun addAlertScreenEvent(clientApiAlertType: ClientApiAlert) {
        inBackground(dispatcher) { coreEventRepository.addEventToCurrentSession(AlertScreenEvent(timeHelper.now(), clientApiAlertType.fromAlertToAlertTypeEvent())) }
    }

    override suspend fun addCompletionCheckEvent(complete: Boolean) {
        inBackground(dispatcher) { coreEventRepository.addEventToCurrentSession(CompletionCheckEvent(timeHelper.now(), complete)) }
    }

    override suspend fun addSuspiciousIntentEvent(unexpectedExtras: Map<String, Any?>) {
        inBackground(dispatcher) { coreEventRepository.addEventToCurrentSession(SuspiciousIntentEvent(timeHelper.now(), unexpectedExtras)) }
    }

    override suspend fun addInvalidIntentEvent(action: String, extras: Map<String, Any?>) {
        inBackground(dispatcher) { coreEventRepository.addEventToCurrentSession(InvalidIntentEvent(timeHelper.now(), action, extras)) }
    }

    override suspend fun getCurrentSessionId(): String = coreEventRepository.getCurrentCaptureSessionEvent().id

    override suspend fun isCurrentSessionAnIdentificationOrEnrolment(): Boolean {
        val session = coreEventRepository.getCurrentCaptureSessionEvent()
        return coreEventRepository.loadEvents(session.id).toList().any {
            it is IdentificationCalloutEvent || it is EnrolmentCalloutEvent
        }
    }
}

fun ClientApiAlert.fromAlertToAlertTypeEvent(): CoreAlertScreenEventType =
    when (this) {
        ClientApiAlert.INVALID_STATE_FOR_INTENT_ACTION -> CoreAlertScreenEventType.INVALID_INTENT_ACTION
        ClientApiAlert.INVALID_METADATA -> CoreAlertScreenEventType.INVALID_METADATA
        ClientApiAlert.INVALID_MODULE_ID -> CoreAlertScreenEventType.INVALID_MODULE_ID
        ClientApiAlert.INVALID_PROJECT_ID -> CoreAlertScreenEventType.INVALID_PROJECT_ID
        ClientApiAlert.INVALID_SELECTED_ID -> CoreAlertScreenEventType.INVALID_SELECTED_ID
        ClientApiAlert.INVALID_SESSION_ID -> CoreAlertScreenEventType.INVALID_SESSION_ID
        ClientApiAlert.INVALID_USER_ID -> CoreAlertScreenEventType.INVALID_USER_ID
        ClientApiAlert.INVALID_VERIFY_ID -> CoreAlertScreenEventType.INVALID_VERIFY_ID
        ClientApiAlert.ROOTED_DEVICE -> CoreAlertScreenEventType.UNEXPECTED_ERROR
    }
