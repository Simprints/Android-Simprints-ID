package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.Constants.RETURN_FOR_FLOW_COMPLETED
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.odk.OdkAction.*
import com.simprints.clientapi.activities.odk.OdkAction.OdkActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.activities.odk.OdkAction.OdkActionFollowUpAction.EnrolLastBiometrics
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.domain.responses.entities.MatchConfidence.*
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.exceptions.InvalidIntentActionException
import com.simprints.clientapi.extensions.isFlowCompletedWithCurrentError
import com.simprints.clientapi.tools.DeviceManager
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.logging.LoggingConstants.CrashReportingCustomKeys.SESSION_ID
import com.simprints.logging.Simber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OdkPresenter(
    private val view: OdkContract.View,
    private val action: OdkAction,
    private val sessionEventsManager: ClientApiSessionEventsManager,
    deviceManager: DeviceManager
) : RequestPresenter(
    view,
    sessionEventsManager,
    deviceManager
), OdkContract.Presenter {

    override suspend fun start() {
        if (action !is OdkActionFollowUpAction) {
            val sessionId = sessionEventsManager.createSession(IntegrationInfo.ODK)
            Simber.tag(SESSION_ID, true).i(sessionId)
        }

        runIfDeviceIsNotRooted {
            when (action) {
                Enrol -> processEnrolRequest()
                Identify -> processIdentifyRequest()
                Verify -> processVerifyRequest()
                ConfirmIdentity -> processConfirmIdentityRequest()
                EnrolLastBiometrics -> processEnrolLastBiometrics()
                Invalid -> throw InvalidIntentActionException()
            }.safeSealedWhens
        }
    }

    override fun handleEnrolResponse(enrol: EnrolResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)

            // There is a bug that creates a malformed session on multiple callouts
            // of EnrolLastBiometrics, this tries to fix that, here is a ticket detailing the issue:
            // https://simprints.atlassian.net/browse/CORE-612
            if (action != EnrolLastBiometrics) {
                sessionEventsManager.closeCurrentSessionNormally()
            }

            view.returnRegistration(enrol.guid, currentSessionId, flowCompletedCheck)
        }
    }

    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnIdentification(
                identify.identifications.getIdsString(),
                identify.identifications.getConfidencesScoresString(),
                identify.identifications.getTiersString(),
                identify.sessionId,
                identify.identifications.getConfidencesFlagsString(),
                getMatchConfidenceForHighestResult(identify.identifications).name,
                flowCompletedCheck
            )
        }
    }

    override fun handleConfirmationResponse(response: ConfirmationResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)

            view.returnConfirmation(flowCompletedCheck, currentSessionId)
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnVerification(
                verify.matchResult.guidFound,
                verify.matchResult.confidenceScore.toString(),
                verify.matchResult.tier.toString(),
                currentSessionId,
                flowCompletedCheck
            )
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnExitForm(refusalForm.reason, refusalForm.extra, currentSessionId, flowCompletedCheck)
        }
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = errorResponse.isFlowCompletedWithCurrentError()
            sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnErrorToClient(errorResponse, flowCompletedCheck, currentSessionId)
        }
    }

    private fun getMatchConfidenceForHighestResult(identifications: List<MatchResult>) =
        when {
            identifications.any { it.matchConfidence == HIGH } -> HIGH
            identifications.any { it.matchConfidence == MEDIUM } -> MEDIUM
            identifications.any { it.matchConfidence == LOW } -> LOW
            else -> NONE
        }

    private suspend fun addCompletionCheckEvent(flowCompletedCheck: Boolean) =
        sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)
}
