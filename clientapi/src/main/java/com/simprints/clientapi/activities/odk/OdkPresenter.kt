package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.extensions.getConfidencesString
import com.simprints.clientapi.extensions.getIdsString
import com.simprints.clientapi.extensions.getTiersString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OdkPresenter(private val view: OdkContract.View,
                   private val action: String?,
                   private val sessionEventsManager: ClientApiSessionEventsManager,
                   private val crashReportManager: ClientApiCrashReportManager)
    : RequestPresenter(view, sessionEventsManager), OdkContract.Presenter {

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

        when (action) {
            ACTION_REGISTER -> processEnrollRequest()
            ACTION_IDENTIFY -> processIdentifyRequest()
            ACTION_VERIFY -> processVerifyRequest()
            ACTION_CONFIRM_IDENTITY -> processConfirmIdentifyRequest()
            else -> view.handleClientRequestError(ClientApiAlert.INVALID_CLIENT_REQUEST)
        }
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            sessionEventsManager.addSkipCheckEvent(errorResponse.skipCheckAfterError())
            view.returnErrorToClient(errorResponse)
        }
    }

    override fun handleEnrollResponse(enroll: EnrollResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = true
            addSkipCheckEvent(skipCheck)
            view.returnRegistration(enroll.guid, skipCheck)
        }
    }

    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = true
            addSkipCheckEvent(skipCheck)
            view.returnIdentification(
                identify.identifications.getIdsString(),
                identify.identifications.getConfidencesString(),
                identify.identifications.getTiersString(),
                identify.sessionId,
                skipCheck
            )
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = true
            addSkipCheckEvent(skipCheck)
            view.returnVerification(
                verify.matchResult.guidFound,
                verify.matchResult.confidence.toString(),
                verify.matchResult.tier.toString(),
                skipCheck
            )
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = true
            addSkipCheckEvent(skipCheck)
            view.returnExitForm(refusalForm.reason, refusalForm.extra, skipCheck)
        }
    }

    private suspend fun addSkipCheckEvent(skipCheck: Boolean) =
        sessionEventsManager.addSkipCheckEvent(skipCheck)

    override fun handleConfirmationResponse(response: ConfirmationResponse) {
        view.returnConfirmation(response.identificationOutcome)
    }

}
