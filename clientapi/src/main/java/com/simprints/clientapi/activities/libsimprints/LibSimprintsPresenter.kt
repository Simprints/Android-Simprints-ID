package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.Constants
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.extensions.skipCheckForError
import com.simprints.libsimprints.Constants.*
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Verification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LibSimprintsPresenter(private val view: LibSimprintsContract.View,
                            private val action: String?,
                            private val sessionEventsManager: ClientApiSessionEventsManager,
                            private val crashReportManager: ClientApiCrashReportManager) :
    RequestPresenter(view, sessionEventsManager), LibSimprintsContract.Presenter {

    override suspend fun start() {
        if (action != SIMPRINTS_SELECT_GUID_INTENT) {
            val sessionId = sessionEventsManager.createSession(IntegrationInfo.STANDARD)
            crashReportManager.setSessionIdCrashlyticsKey(sessionId)
        }

        when (action) {
            SIMPRINTS_REGISTER_INTENT -> processEnrollRequest()
            SIMPRINTS_IDENTIFY_INTENT -> processIdentifyRequest()
            SIMPRINTS_VERIFY_INTENT -> processVerifyRequest()
            SIMPRINTS_SELECT_GUID_INTENT -> processConfirmIdentifyRequest()
            else -> view.handleClientRequestError(ClientApiAlert.INVALID_CLIENT_REQUEST)
        }
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = errorResponse.skipCheckForError()
            addSkipCheckEvent(skipCheck)
            view.returnErrorToClient(errorResponse, skipCheck)
        }
    }

    override fun handleEnrollResponse(enroll: EnrollResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = Constants.SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
            addSkipCheckEvent(skipCheck)
            view.returnRegistration(Registration(enroll.guid), skipCheck)
        }
    }


    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = Constants.SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
            addSkipCheckEvent(skipCheck)
            view.returnIdentification(ArrayList(identify.identifications.map {
                Identification(
                    it.guidFound,
                    it.confidence,
                    it.tier.fromDomainToLibsimprintsTier())
            }), identify.sessionId, skipCheck)
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = Constants.SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
            addSkipCheckEvent(skipCheck)
            with(verify) {
                val verification = Verification(
                    matchResult.confidence,
                    matchResult.tier.fromDomainToLibsimprintsTier(),
                    matchResult.guidFound)
                view.returnVerification(verification, skipCheck)
            }
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = Constants.SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
            addSkipCheckEvent(skipCheck)
            view.returnRefusalForms(RefusalForm(refusalForm.reason, refusalForm.extra), skipCheck)
        }
    }

    private suspend fun addSkipCheckEvent(skipCheck: Boolean) =
        sessionEventsManager.addSkipCheckEvent(skipCheck)
}

