package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.Constants.RETURN_FOR_FLOW_COMPLETED
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.extensions.isFlowCompletedWithCurrentError
import com.simprints.clientapi.tools.DeviceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OdkPresenter(
    private val view: OdkContract.View,
    private val action: String?,
    private val sessionEventsManager: ClientApiSessionEventsManager,
    deviceManager: DeviceManager,
    crashReportManager: ClientApiCrashReportManager
) : RequestPresenter(
    view,
    sessionEventsManager,
    deviceManager,
    crashReportManager
), OdkContract.Presenter {

    companion object {
        private const val PACKAGE_NAME = "com.simprints.simodkadapter"
        const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
        const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"
    }

    override suspend fun start() {
        if (action != ACTION_CONFIRM_IDENTITY) {
            val sessionId = sessionEventsManager.createSession(IntegrationInfo.ODK)
            crashReportManager.setSessionIdCrashlyticsKey(sessionId)
        }

        runIfDeviceIsNotRooted {
            when (action) {
                ACTION_REGISTER -> processEnrollRequest()
                ACTION_IDENTIFY -> processIdentifyRequest()
                ACTION_VERIFY -> processVerifyRequest()
                ACTION_CONFIRM_IDENTITY -> processConfirmIdentityRequest()
                else -> view.handleClientRequestError(ClientApiAlert.INVALID_CLIENT_REQUEST)
            }
        }
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = errorResponse.isFlowCompletedWithCurrentError()
            sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)
            view.returnErrorToClient(errorResponse, flowCompletedCheck, getCurrentSessionId())
        }
    }

    override fun handleEnrollResponse(enroll: EnrollResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnRegistration(enroll.guid, getCurrentSessionId(), flowCompletedCheck)
        }
    }

    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnIdentification(
                identify.identifications.getIdsString(),
                identify.identifications.getConfidencesString(),
                identify.identifications.getTiersString(),
                identify.sessionId,
                flowCompletedCheck
            )
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnVerification(
                verify.matchResult.guidFound,
                verify.matchResult.confidence.toString(),
                verify.matchResult.tier.toString(),
                getCurrentSessionId(),
                flowCompletedCheck
            )
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnExitForm(refusalForm.reason, refusalForm.extra, getCurrentSessionId(), flowCompletedCheck)
        }
    }

    private suspend fun getCurrentSessionId() = sessionEventsManager.getCurrentSessionId() ?: ""

    private suspend fun addCompletionCheckEvent(flowCompletedCheck: Boolean) =
        sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)

    override fun handleConfirmationResponse(response: ConfirmationResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnConfirmation(flowCompletedCheck, getCurrentSessionId())
        }
    }
}
