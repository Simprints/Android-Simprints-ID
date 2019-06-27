package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.domain.responses.EnrollResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.RefusalFormResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier


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
        if(action != ACTION_CONFIRM_IDENTITY) {
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

    override fun handleEnrollResponse(enroll: EnrollResponse) =
        view.returnRegistration(enroll.guid, false)

    override fun handleIdentifyResponse(identify: IdentifyResponse) {
        sharedPreferencesManager.stashSessionId(identify.sessionId)
        view.returnIdentification(ArrayList(identify.identifications.map {
            Identification(it.guidFound, it.confidence, Tier.valueOf(it.tier.name))
        }), identify.sessionId)
    }

    override fun handleVerifyResponse(verify: VerifyResponse) = view.returnVerification(
        verify.matchResult.confidence,
        Tier.valueOf(verify.matchResult.tier.name),
        verify.matchResult.guidFound,
        false
    )

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) =
        view.returnRefusalForms(refusalForm.reason, refusalForm.extra, false)

    private fun checkAndProcessSessionId() {
        if ((view.extras?.get(Constants.SIMPRINTS_SESSION_ID) as CharSequence?).isNullOrBlank()) {
            if (sharedPreferencesManager.peekSessionId().isNotBlank()) {
                view.injectSessionIdIntoIntent(sharedPreferencesManager.popSessionId())
            }
        }

        processConfirmIdentifyRequest()
    }

}
