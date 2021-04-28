package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.Constants
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.*
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.LibSimprintsActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.LibSimprintsActionFollowUpAction.EnrolLastBiometrics
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.exceptions.InvalidIntentActionException
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
        if (action !is LibSimprintsActionFollowUpAction) {
            val sessionId = sessionEventsManager.createSession(IntegrationInfo.STANDARD)
            crashReportManager.setSessionIdCrashlyticsKey(sessionId)
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

            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnRegistration(Registration(enrol.guid), currentSessionId, flowCompletedCheck)
        }
    }

    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            view.returnIdentification(ArrayList(identify.identifications.map {
                Identification(
                    it.guidFound,
                    it.confidenceScore,
                    it.tier.fromDomainToLibsimprintsTier()
                )
            }), identify.sessionId, flowCompletedCheck)
        }
    }


    override fun handleConfirmationResponse(response: ConfirmationResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnConfirmation(flowCompletedCheck, currentSessionId)
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            with(verify) {
                val verification = Verification(
                    matchResult.confidenceScore,
                    matchResult.tier.fromDomainToLibsimprintsTier(),
                    matchResult.guidFound
                )
                view.returnVerification(verification, currentSessionId, flowCompletedCheck)
            }
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = Constants.RETURN_FOR_FLOW_COMPLETED
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnRefusalForms(
                RefusalForm(refusalForm.reason, refusalForm.extra),
                currentSessionId, flowCompletedCheck
            )
        }
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            // need to get sessionId before it is closed and null
            val currentSessionId = sessionEventsManager.getCurrentSessionId()

            val flowCompletedCheck = errorResponse.isFlowCompletedWithCurrentError()
            addCompletionCheckEvent(flowCompletedCheck)
            sessionEventsManager.closeCurrentSessionNormally()

            view.returnErrorToClient(errorResponse, flowCompletedCheck, currentSessionId)
        }
    }


    private suspend fun addCompletionCheckEvent(flowCompletedCheck: Boolean) =
        sessionEventsManager.addCompletionCheckEvent(flowCompletedCheck)
}

