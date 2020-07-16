package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.controllers.core.eventData.model.fromDomainToCore
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.core.tools.extentions.inBackground
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.events.*
import com.simprints.libsimprints.BuildConfig
import kotlinx.coroutines.runBlocking
import com.simprints.id.data.db.event.domain.events.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType as CoreAlertScreenEventType

class ClientApiSessionEventsManagerImpl(private val coreEventRepository: EventRepository,
                                        private val timeHelper: ClientApiTimeHelper) :
    ClientApiSessionEventsManager {

    override suspend fun createSession(integration: IntegrationInfo): String {
        runBlocking {
            coreEventRepository.createSession(BuildConfig.VERSION_NAME)
        }

        inBackground { coreEventRepository.addEvent(IntentParsingEvent(timeHelper.now(), integration.fromDomainToCore())) }

        return coreEventRepository.getCurrentCaptureSessionEvent().id
    }

    override suspend fun addAlertScreenEvent(clientApiAlertType: ClientApiAlert) {
        inBackground { coreEventRepository.addEvent(AlertScreenEvent(timeHelper.now(), clientApiAlertType.fromAlertToAlertTypeEvent())) }
    }

    override suspend fun addCompletionCheckEvent(complete: Boolean) {
        inBackground { coreEventRepository.addEvent(CompletionCheckEvent(timeHelper.now(), complete)) }
    }

    override suspend fun addSuspiciousIntentEvent(unexpectedExtras: Map<String, Any?>) {
        inBackground { coreEventRepository.addEvent(SuspiciousIntentEvent(timeHelper.now(), unexpectedExtras)) }
    }

    override suspend fun addInvalidIntentEvent(action: String, extras: Map<String, Any?>) {
        inBackground { coreEventRepository.addEvent(InvalidIntentEvent(timeHelper.now(), action, extras)) }
    }

    override suspend fun getCurrentSessionId(): String = coreEventRepository.getCurrentCaptureSessionEvent().id

    override suspend fun isCurrentSessionAnIdentification(): Boolean {
        val sessionId = coreEventRepository.getCurrentCaptureSessionEvent()
        //coreEventRepository..getEvents().filterIsInstance(IdentificationCalloutEvent::class.java).isNotEmpty()
        return false //STOPSHIP
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
