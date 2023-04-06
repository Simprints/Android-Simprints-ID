package com.simprints.clientapi.controllers.core.eventData

import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.controllers.core.eventData.model.fromDomainToCore
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.core.ExternalScope
import com.simprints.core.domain.workflow.WorkflowCacheClearer
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.canCoSyncAllData
import com.simprints.infra.config.domain.models.canCoSyncAnalyticsData
import com.simprints.infra.config.domain.models.canCoSyncBiometricData
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.*
import com.simprints.infra.events.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType as CoreAlertScreenEventType

class ClientApiSessionEventsManagerImpl @Inject constructor(
    private val coreEventRepository: EventRepository,
    private val timeHelper: ClientApiTimeHelper,
    private val workflowCacheClearer: WorkflowCacheClearer,
    private val configManager: ConfigManager,
    @ExternalScope private val externalScope: CoroutineScope
) : ClientApiSessionEventsManager {

    override suspend fun createSession(integration: IntegrationInfo): String {
        // Clear cached steps before creating a new session
        workflowCacheClearer.clearSteps()
        coreEventRepository.createSession()

        externalScope.launch {
            coreEventRepository.addOrUpdateEvent(
                IntentParsingEvent(
                    timeHelper.now(),
                    integration.fromDomainToCore()
                )
            )
        }

        return getCurrentSessionId()
    }

    override suspend fun addAlertScreenEvent(clientApiAlertType: ClientApiAlert) {
        externalScope.launch {
            coreEventRepository.addOrUpdateEvent(
                AlertScreenEvent(
                    timeHelper.now(),
                    clientApiAlertType.fromAlertToAlertTypeEvent()
                )
            )
        }
    }

    /**
     * Since this is the last event before returning to the calling app, we want to make sure that all previous events
     * were saved already. That's why this is blocking instead of running in background (`inBackground(dispatcher)`).
     * This should give enough time to previous events to be saved since SQLite queues write attempts.
     * [https://www.sqlite.org/lockingv3.html]
     */
    override suspend fun addCompletionCheckEvent(complete: Boolean) {
        coreEventRepository.addOrUpdateEvent(
            CompletionCheckEvent(
                timeHelper.now(),
                complete
            )
        )
    }

    override suspend fun addSuspiciousIntentEvent(unexpectedExtras: Map<String, Any?>) {
        externalScope.launch {
            coreEventRepository.addOrUpdateEvent(
                SuspiciousIntentEvent(
                    timeHelper.now(),
                    unexpectedExtras
                )
            )
        }
    }

    override suspend fun addInvalidIntentEvent(action: String, extras: Map<String, Any?>) {
        externalScope.launch {
            coreEventRepository.addOrUpdateEvent(
                InvalidIntentEvent(
                    timeHelper.now(),
                    action,
                    extras
                )
            )
        }
    }

    override suspend fun getCurrentSessionId(): String =
        coreEventRepository.getCurrentCaptureSessionEvent().id

    override suspend fun isCurrentSessionAnIdentificationOrEnrolment(): Boolean {
        val session = coreEventRepository.getCurrentCaptureSessionEvent()
        return coreEventRepository.observeEventsFromSession(session.id).toList().any {
            it is IdentificationCalloutEvent || it is EnrolmentCalloutEvent
        }
    }

    override suspend fun isSessionHasIdentificationCallback(sessionId: String): Boolean {
        val events = coreEventRepository.observeEventsFromSession(sessionId)
        return events.toList().any {
            it is IdentificationCallbackEvent
        }
    }

    override suspend fun getAllEventsForSession(sessionId: String): Flow<Event> =
        when {
            configManager.getProjectConfiguration().canCoSyncAllData() -> {
                coreEventRepository.observeEventsFromSession(sessionId)
            }
            configManager.getProjectConfiguration().canCoSyncBiometricData() -> {
                coreEventRepository.observeEventsFromSession(sessionId).filter {
                    it is EnrolmentEventV2 || it is PersonCreationEvent || it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent
                }
            }
            configManager.getProjectConfiguration().canCoSyncAnalyticsData() -> {
                coreEventRepository.observeEventsFromSession(sessionId).filterNot {
                    it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent
                }
            }
            else -> {
                emptyFlow()
            }
        }

    override suspend fun deleteSessionEvents(sessionId: String) {
        externalScope.launch {
            coreEventRepository.deleteSessionEvents(sessionId)
        }
    }

    /**
     * Closes the current session normally, without adding an [ArtificialTerminationEvent].
     * After calling this method, the currentSessionId will be `null` and calling [getCurrentSessionId] will open
     * a new session.
     *
     * Since this is updating the session, it needs to run blocking instead of in background.
     */
    override suspend fun closeCurrentSessionNormally() {
        coreEventRepository.closeCurrentSession()
    }

    private fun ClientApiAlert.fromAlertToAlertTypeEvent(): CoreAlertScreenEventType =
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
}
