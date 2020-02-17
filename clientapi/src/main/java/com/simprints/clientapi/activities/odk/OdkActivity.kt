package com.simprints.clientapi.activities.odk

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.identity.OdkGuidSelectionNotifier
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class OdkActivity : RequestActivity(), OdkContract.View {

    companion object {
        private const val ODK_REGISTRATION_ID_KEY = "odk-registration-id"
        private const val ODK_GUIDS_KEY = "odk-guids"
        private const val ODK_BIOMETRICS_COMPLETE_CHECK_KEY = "odk-biometrics-complete"
        private const val ODK_CONFIDENCES_KEY = "odk-confidences"
        private const val ODK_TIERS_KEY = "odk-tiers"
        private const val ODK_SESSION_ID = "odk-session-id"
        private const val ODK_EXIT_REASON = "odk-exit-reason"
        private const val ODK_EXIT_EXTRA = "odk-exit-extra"
        private const val CONFIRM_IDENTITY_ACTION = "com.simprints.simodkadapter.CONFIRM_IDENTITY"
    }

    override val presenter: OdkContract.Presenter by inject { parametersOf(this, action) }

    override val guidSelectionNotifier: OdkGuidSelectionNotifier by inject {
        parametersOf(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action != CONFIRM_IDENTITY_ACTION)
            showLauncherScreen()

        loadClientApiKoinModules()
    }

    override fun returnRegistration(registrationId: String, sessionId: String, flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_REGISTRATION_ID_KEY, registrationId)
        it.putExtra(ODK_SESSION_ID, sessionId)
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)

        sendOkResult(it)
    }

    override fun returnIdentification(idList: String,
                                      confidenceList: String,
                                      tierList: String,
                                      sessionId: String,
                                      flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_GUIDS_KEY, idList)
        it.putExtra(ODK_CONFIDENCES_KEY, confidenceList)
        it.putExtra(ODK_TIERS_KEY, tierList)
        it.putExtra(ODK_SESSION_ID, sessionId)
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)

        sendOkResult(it)
    }

    override fun returnVerification(id: String, confidence: String, tier: String, sessionId: String, flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_GUIDS_KEY, id)
        it.putExtra(ODK_CONFIDENCES_KEY, confidence)
        it.putExtra(ODK_TIERS_KEY, tier)
        it.putExtra(ODK_SESSION_ID, sessionId)
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)

        sendOkResult(it)
    }

    override fun returnExitForm(reason: String, extra: String, sessionId: String, flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_EXIT_REASON, reason)
        it.putExtra(ODK_EXIT_EXTRA, extra)
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)
        it.putExtra(ODK_SESSION_ID, sessionId)

        sendOkResult(it)
    }

    override fun returnConfirmation(flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)
        sendOkResult(it)
    }

    override fun returnErrorToClient(errorResponse: ErrorResponse,
                                     flowCompletedCheck: Boolean,
                                     sessionId: String) = Intent().let {
        it.putExtra(ODK_BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck)
        it.putExtra(ODK_SESSION_ID, sessionId)

        sendOkResult(it)
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadClientApiKoinModules()
    }

}
