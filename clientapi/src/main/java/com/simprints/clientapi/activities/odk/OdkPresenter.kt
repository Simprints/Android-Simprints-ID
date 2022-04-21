package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.Constants.RETURN_FOR_FLOW_COMPLETED
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.odk.OdkAction.Enrol
import com.simprints.clientapi.activities.odk.OdkAction.Identify
import com.simprints.clientapi.activities.odk.OdkAction.Invalid
import com.simprints.clientapi.activities.odk.OdkAction.OdkActionFollowUpAction
import com.simprints.clientapi.activities.odk.OdkAction.OdkActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.activities.odk.OdkAction.OdkActionFollowUpAction.EnrolLastBiometrics
import com.simprints.clientapi.activities.odk.OdkAction.Verify
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.domain.responses.ConfirmationResponse
import com.simprints.clientapi.domain.responses.EnrolResponse
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.RefusalFormResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.domain.responses.entities.MatchConfidence.HIGH
import com.simprints.clientapi.domain.responses.entities.MatchConfidence.LOW
import com.simprints.clientapi.domain.responses.entities.MatchConfidence.MEDIUM
import com.simprints.clientapi.domain.responses.entities.MatchConfidence.NONE
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
    deviceManager: DeviceManager,
    sharedPreferencesManager: SharedPreferencesManager
) : RequestPresenter(
    view = view,
    eventsManager = sessionEventsManager,
    deviceManager = deviceManager,
    sharedPreferencesManager = sharedPreferencesManager,
    sessionEventsManager = sessionEventsManager
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
            // As requested in https://simprints.atlassian.net/browse/CORE-987
            // we should close the session after any enrollment request
            sessionEventsManager.closeCurrentSessionNormally()

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
