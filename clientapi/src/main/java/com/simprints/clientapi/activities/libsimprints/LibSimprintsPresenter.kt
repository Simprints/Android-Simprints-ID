package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.Constants
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.*
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.extensions.isFlowCompletedWithCurrentError
import com.simprints.clientapi.tools.DeviceManager
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Verification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LibSimprintsPresenter(
    private val view: LibSimprintsContract.View,
    private val action: LibSimprintsAction,
    private val sessionEventsManager: ClientApiSessionEventsManager,
    deviceManager: DeviceManager,
    crashReportManager: ClientApiCrashReportManager
) : RequestPresenter(
    view,
    sessionEventsManager,
    deviceManager,
    crashReportManager
), LibSimprintsContract.Presenter {

    override suspend fun start() {
        if (action != ConfirmIdentity) {
            val sessionId = sessionEventsManager.createSession(IntegrationInfo.STANDARD)
            crashReportManager.setSessionIdCrashlyticsKey(sessionId)
        }

        runIfDeviceIsNotRooted {
            when (action) {
                Register -> processEnrollRequest()
                Identify -> processIdentifyRequest()
                Verify -> processVerifyRequest()
                ConfirmIdentity -> processConfirmIdentityRequest()
                Invalid -> view.handleClientRequestError(ClientApiAlert.INVALID_CLIENT_REQUEST)
            }.safeSealedWhens
        }
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = errorResponse.isFlowCompletedWithCurrentError()
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnErrorToClient(errorResponse, flowCompletedCheck, getCurrentSessionIdOrEmpty())
        }
    }

    override fun handleEnrollResponse(enroll: EnrollResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnRegistration(Registration(enroll.guid), getCurrentSessionIdOrEmpty(), flowCompletedCheck)
        }
    }


    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnIdentification(ArrayList(identify.identifications.map {
                Identification(
                    it.guidFound,
                    it.confidence,
                    it.tier.fromDomainToLibsimprintsTier())
            }), identify.sessionId, flowCompletedCheck)
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            with(verify) {
                val verification = Verification(
                    matchResult.confidence,
                    matchResult.tier.fromDomainToLibsimprintsTier(),
                    matchResult.guidFound)
                view.returnVerification(verification, getCurrentSessionIdOrEmpty(), flowCompletedCheck)
            }
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnRefusalForms(RefusalForm(refusalForm.reason, refusalForm.extra),
                getCurrentSessionIdOrEmpty(), flowCompletedCheck)
        }
    }

    private suspend fun getCurrentSessionIdOrEmpty() = sessionEventsManager.getCurrentSessionId() ?: ""

    private suspend fun addCompletionCheckEvent(flowCompletedCheck: Boolean) =
        sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)

    override fun handleConfirmationResponse(response: ConfirmationResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnConfirmation(flowCompletedCheck, getCurrentSessionIdOrEmpty())
        }
    }
}

