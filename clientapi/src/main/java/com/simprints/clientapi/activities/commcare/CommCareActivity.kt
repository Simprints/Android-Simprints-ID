package com.simprints.clientapi.activities.commcare

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.identity.CommCareGuidSelectionNotifier
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class CommCareActivity : RequestActivity(), CommCareContract.View {

    companion object {
        private const val COMMCARE_BUNDLE_KEY = "odk_intent_bundle"

        // Based on the documentation, we are supposed to send either odk_intent_bundle (for key-values result)
        // or odk_intent_data (for a single integer or string), but apparently due to a bug in commcare
        // if we send `odk_intent_bundle` only, the result is processed correctly, but a toast shows an
        // error message. That is because commcare can't find odk_intent_data
        private const val COMMCARE_DATA_KEY = "odk_intent_data"

        private const val BIOMETRICS_COMPLETE_CHECK_KEY = "biometricsComplete"
        private const val REGISTRATION_GUID_KEY = "guid"
        private const val VERIFICATION_CONFIDENCE_KEY = "confidence"
        private const val VERIFICATION_TIER_KEY = "tier"
        private const val VERIFICATION_GUID_KEY = "guid"
        private const val EXIT_REASON = "exitReason"
        private const val EXIT_EXTRA = "exitExtra"
        private const val SIMPRINTS_SESSION_ID = "sessionId"

        private const val CONFIRM_IDENTITY_ACTION = "com.simprints.commcare.CONFIRM_IDENTITY"
    }

    override val presenter: CommCareContract.Presenter by inject { parametersOf(this, action) }

    override val guidSelectionNotifier: CommCareGuidSelectionNotifier by inject {
        parametersOf(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action != CONFIRM_IDENTITY_ACTION)
            showLauncherScreen()

        loadClientApiKoinModules()
    }

    override fun returnRegistration(guid: String, sessionId: String, flowCompletedCheck: Boolean) = Intent().let {
        val data = Bundle().apply {
            putString(BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck.toString())
            putString(SIMPRINTS_SESSION_ID, sessionId)
            putString(REGISTRATION_GUID_KEY, guid)
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    override fun returnVerification(confidence: Int, tier: Tier, guid: String, sessionId: String, flowCompletedCheck: Boolean) = Intent().let {
        val data = Bundle().apply {
            putString(SIMPRINTS_SESSION_ID, sessionId)
            putString(BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck.toString())
            putString(VERIFICATION_CONFIDENCE_KEY, confidence.toString())
            putString(VERIFICATION_TIER_KEY, tier.name)
            putString(VERIFICATION_GUID_KEY, guid)
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    override fun returnExitForms(reason: String,
                                 extra: String,
                                 flowCompletedCheck: Boolean) = Intent().let {

        val data = Bundle().apply {
            putString(BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck.toString())
            putString(EXIT_REASON, reason)
            putString(EXIT_EXTRA, extra)
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    override fun returnConfirmation(flowCompletedCheck: Boolean) = Intent().let {
        val data = Bundle().apply {
            putString(BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck.toString())
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    override fun returnErrorToClient(errorResponse: ErrorResponse,
                                     flowCompletedCheck: Boolean) = Intent().let {

        val data = Bundle().apply {
            putString(BIOMETRICS_COMPLETE_CHECK_KEY, flowCompletedCheck.toString())
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    override fun returnIdentification(identifications: ArrayList<Identification>,
                                      sessionId: String) = Intent().let {

        // CommCare can't process Identifications as standard CommCare Bundle (COMMCARE_BUNDLE_KEY).
        // It's excepting Identifications results in the LibSimprints format.
        it.putParcelableArrayListExtra(Constants.SIMPRINTS_IDENTIFICATIONS, identifications)
        it.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)

        sendOkResult(it)
    }

    private fun injectDataAsCommCareBundleIntoIntent(intent: Intent, data: Bundle) {
        intent.putExtra(COMMCARE_BUNDLE_KEY, data)
        intent.putExtra(COMMCARE_DATA_KEY, "")
    }

    override fun injectSessionIdIntoIntent(sessionId: String) {
        intent.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadClientApiKoinModules()
    }

}
