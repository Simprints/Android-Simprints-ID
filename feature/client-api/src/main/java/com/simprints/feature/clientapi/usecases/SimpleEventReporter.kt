package com.simprints.feature.clientapi.usecases

import com.simprints.core.ExternalScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.feature.orchestrator.models.ActionRequest
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent
import com.simprints.infra.events.event.domain.models.ConnectivitySnapshotEvent
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent
import com.simprints.infra.events.event.domain.models.callout.ConfirmationCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.EnrolmentLastBiometricsCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.IdentificationCalloutEvent
import com.simprints.infra.events.event.domain.models.callout.VerificationCalloutEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SimpleEventReporter @Inject constructor(
    private val coreEventRepository: EventRepository,
    private val timeHelper: TimeHelper,
    private val simNetworkUtils: SimNetworkUtils,
    @ExternalScope private val externalScope: CoroutineScope
) {

    fun addUnknownExtrasEvent(unknownExtras: Map<String, Any?>) {
        if (unknownExtras.isNotEmpty()) {
            externalScope.launch {
                coreEventRepository.addOrUpdateEvent(SuspiciousIntentEvent(timeHelper.now(), unknownExtras))
            }
        }
    }

    suspend fun addConnectivityStateEvent() {
        coreEventRepository.addOrUpdateEvent(ConnectivitySnapshotEvent(timeHelper.now(), simNetworkUtils.connectionsStates))
    }

    suspend fun addRequestActionEvent(request: ActionRequest) {
        val startTime = timeHelper.now()
        val event = with(request) {
            when (this) {
                is ActionRequest.EnrolActionRequest -> EnrolmentCalloutEvent(startTime, projectId, userId, moduleId, metadata)
                is ActionRequest.IdentifyActionRequest -> IdentificationCalloutEvent(startTime, projectId, userId, moduleId, metadata)
                is ActionRequest.VerifyActionRequest -> VerificationCalloutEvent(startTime, projectId, userId, moduleId, verifyGuid, metadata)
                is ActionRequest.ConfirmActionRequest -> ConfirmationCalloutEvent(startTime, projectId, selectedGuid, sessionId)
                is ActionRequest.EnrolLastBiometricActionRequest -> EnrolmentLastBiometricsCalloutEvent(startTime, projectId, userId, moduleId, metadata, sessionId)
            }
        }
        coreEventRepository.addOrUpdateEvent(event)
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

}

