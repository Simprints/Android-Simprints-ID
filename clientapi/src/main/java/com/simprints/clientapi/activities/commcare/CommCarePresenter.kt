package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.Constants.SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.domain.responses.*
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CommCarePresenter(private val view: CommCareContract.View,
                        private val action: String?,
                        private val sessionEventsManager: ClientApiSessionEventsManager,
                        private val crashReportManager: ClientApiCrashReportManager,
                        private val sharedPreferencesManager: SharedPreferencesManager)
    : RequestPresenter(view, sessionEventsManager), CommCareContract.Presenter {

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

        when (action) {
            ACTION_REGISTER -> processEnrollRequest()
            ACTION_IDENTIFY -> processIdentifyRequest()
            ACTION_VERIFY -> processVerifyRequest()
            ACTION_CONFIRM_IDENTITY -> checkAndProcessSessionId()
            else -> view.handleClientRequestError(ClientApiAlert.INVALID_CLIENT_REQUEST)
        }
    }

    override fun handleEnrollResponse(enroll: EnrollResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
            addSkipCheckEvent(skipCheck)
            view.returnRegistration(enroll.guid, skipCheck)
        }
    }

    //CommCare can process Identifications results as LibSimprints format only.
    //So CC will be able to handle skipCheck flag for Identifications only when libsimprints supports
    // skipValue flag and CC updates the libsimprints version (=never)
    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        sharedPreferencesManager.stashSessionId(identify.sessionId)
        view.returnIdentification(ArrayList(identify.identifications.map {
            Identification(it.guidFound, it.confidence, Tier.valueOf(it.tier.name))
        }), identify.sessionId)
    }

    override fun handleResponseError(errorResponse: ErrorResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            addSkipCheckEvent(errorResponse.skipCheckForError())
            view.returnErrorToClient(errorResponse)
        }
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
            addSkipCheckEvent(skipCheck)
            view.returnVerification(
                verify.matchResult.confidence,
                Tier.valueOf(verify.matchResult.tier.name),
                verify.matchResult.guidFound,
                skipCheck
            )
        }
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        CoroutineScope(Dispatchers.Main).launch {
            val skipCheck = SKIP_CHECK_VALUE_FOR_COMPLETED_FLOW
            addSkipCheckEvent(skipCheck)
            view.returnExitForms(refusalForm.reason, refusalForm.extra, skipCheck)
        }
    }

    private suspend fun addSkipCheckEvent(skipCheck: Boolean) =
        sessionEventsManager.addSkipCheckEvent(skipCheck)

    private fun checkAndProcessSessionId() {
        if ((view.extras?.get(Constants.SIMPRINTS_SESSION_ID) as CharSequence?).isNullOrBlank()) {
            if (sharedPreferencesManager.peekSessionId().isNotBlank()) {
                view.injectSessionIdIntoIntent(sharedPreferencesManager.popSessionId())
            }
        }

        processConfirmIdentifyRequest()
    }

}
