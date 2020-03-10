package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.Constants.RETURN_FOR_FLOW_COMPLETED
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.extensions.isFlowCompletedWithCurrentError
import com.simprints.clientapi.tools.DeviceManager
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommCarePresenter(
    private val view: CommCareContract.View,
    private val action: String?,
    private val sessionEventsManager: ClientApiSessionEventsManager,
    private val sharedPreferencesManager: SharedPreferencesManager,
    deviceManager: DeviceManager,
    crashReportManager: ClientApiCrashReportManager
) : RequestPresenter(
    view,
    sessionEventsManager,
    deviceManager,
    crashReportManager
), CommCareContract.Presenter {

    companion object {
        private const val PACKAGE_NAME = "com.simprints.commcare"
        const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
        const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"
    }

    override suspend fun start() {
        if (action != ACTION_CONFIRM_IDENTITY) {
            val sessionId = sessionEventsManager.createSession(IntegrationInfo.COMMCARE)
            crashReportManager.setSessionIdCrashlyticsKey(sessionId)
        }

        runIfDeviceIsNotRooted {
            when (action) {
                ACTION_REGISTER -> processEnrollRequest()
                ACTION_IDENTIFY -> processIdentifyRequest()
                ACTION_VERIFY -> processVerifyRequest()
                ACTION_CONFIRM_IDENTITY -> checkAndProcessSessionId()
                else -> view.handleClientRequestError(ClientApiAlert.INVALID_CLIENT_REQUEST)
            }
        }
    }

    override fun handleEnrollResponse(enroll: EnrollResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnRegistration(enroll.guid, getCurrentSessionIdOrEmpty(), flowCompletedCheck)
        }
    }

    //CommCare can process Identifications results as LibSimprints format only.
    //So CC will be able to handle flowCompletedCheck flag for Identifications only when libsimprints supports
    // flowCompletedCheck flag and CC updates the libsimprints version (=never)
    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        sharedPreferencesManager.stashSessionId(identify.sessionId)
        view.returnIdentification(ArrayList(identify.identifications.map {
            Identification(it.guidFound, it.confidence, Tier.valueOf(it.tier.name))
        }), identify.sessionId)
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = errorResponse.isFlowCompletedWithCurrentError()
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnErrorToClient(errorResponse, flowCompletedCheck, getCurrentSessionIdOrEmpty())
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnVerification(
                verify.matchResult.confidence,
                Tier.valueOf(verify.matchResult.tier.name),
                verify.matchResult.guidFound,
                getCurrentSessionIdOrEmpty(),
                flowCompletedCheck
            )
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnExitForms(refusalForm.reason, refusalForm.extra,
                getCurrentSessionIdOrEmpty(), flowCompletedCheck)
        }
    }

    private suspend fun getCurrentSessionIdOrEmpty() = sessionEventsManager.getCurrentSessionId()

    private suspend fun addCompletionCheckEvent(flowCompletedCheck: Boolean) =
        sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)

    override fun handleConfirmationResponse(response: ConfirmationResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnConfirmation(flowCompletedCheck, getCurrentSessionIdOrEmpty())
        }
    }

    private suspend fun checkAndProcessSessionId() {
        if ((view.extras?.get(Constants.SIMPRINTS_SESSION_ID) as CharSequence?).isNullOrBlank()) {
            if (sharedPreferencesManager.peekSessionId().isNotBlank()) {
                view.injectSessionIdIntoIntent(sharedPreferencesManager.popSessionId())
            }
        }

        processConfirmIdentityRequest()
    }

}
