package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.controllers.core.eventData.model.fromDomainToCore
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.*
import com.simprints.libsimprints.BuildConfig
import kotlinx.coroutines.runBlocking
import com.simprints.id.data.db.session.domain.models.events.AlertScreenEvent.AlertScreenEventType as CoreAlertScreenEventType

class ClientApiSessionEventsManagerImpl(private val coreSessionRepository: SessionRepository,
                                        private val timeHelper: ClientApiTimeHelper) :
    ClientApiSessionEventsManager {

    override suspend fun createSession(integration: IntegrationInfo): String {
        runBlocking {
            coreSessionRepository.createSession(BuildConfig.VERSION_NAME)
        }

        coreSessionRepository.addEventToCurrentSessionInBackground(IntentParsingEvent(timeHelper.now(), integration.fromDomainToCore()))

        return coreSessionRepository.getCurrentSession().id
    }

    override suspend fun addAlertScreenEvent(clientApiAlertType: ClientApiAlert) {
        coreSessionRepository.addEventToCurrentSessionInBackground(AlertScreenEvent(timeHelper.now(), clientApiAlertType.fromAlertToAlertTypeEvent()))
    }

    override suspend fun addCompletionCheckEvent(complete: Boolean) {
        coreSessionRepository.addEventToCurrentSessionInBackground(CompletionCheckEvent(timeHelper.now(), complete))
    }

    override suspend fun addSuspiciousIntentEvent(unexpectedExtras: Map<String, Any?>) {
        coreSessionRepository.addEventToCurrentSessionInBackground(SuspiciousIntentEvent(timeHelper.now(), unexpectedExtras))
    }

    override suspend fun addInvalidIntentEvent(action: String, extras: Map<String, Any?>) {
        coreSessionRepository.addEventToCurrentSessionInBackground(InvalidIntentEvent(timeHelper.now(), action, extras))
    }

    override suspend fun getCurrentSessionId(): String = coreSessionRepository.getCurrentSession().id
}

fun ClientApiAlert.fromAlertToAlertTypeEvent(): CoreAlertScreenEventType =
    when (this) {
        ClientApiAlert.INVALID_CLIENT_REQUEST -> CoreAlertScreenEventType.INVALID_INTENT_ACTION
        ClientApiAlert.INVALID_METADATA -> CoreAlertScreenEventType.INVALID_METADATA
        ClientApiAlert.INVALID_MODULE_ID -> CoreAlertScreenEventType.INVALID_MODULE_ID
        ClientApiAlert.INVALID_PROJECT_ID -> CoreAlertScreenEventType.INVALID_PROJECT_ID
        ClientApiAlert.INVALID_SELECTED_ID -> CoreAlertScreenEventType.INVALID_SELECTED_ID
        ClientApiAlert.INVALID_SESSION_ID -> CoreAlertScreenEventType.INVALID_SESSION_ID
        ClientApiAlert.INVALID_USER_ID -> CoreAlertScreenEventType.INVALID_USER_ID
        ClientApiAlert.INVALID_VERIFY_ID -> CoreAlertScreenEventType.INVALID_VERIFY_ID
        ClientApiAlert.ROOTED_DEVICE -> CoreAlertScreenEventType.UNEXPECTED_ERROR
    }
