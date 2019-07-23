package com.simprints.clientapi.activities.commcare

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.extensions.skipCheckForError
import com.simprints.clientapi.identity.CommCareGuidSelectionNotifier
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class CommCareActivity : RequestActivity(), CommCareContract.View {

    companion object {
        const val COMMCARE_BUNDLE_KEY = "odk_intent_bundle"

        const val SKIP_CHECK_KEY = "skipCheck"
        const val REGISTRATION_GUID_KEY = "guid"
        const val VERIFICATION_CONFIDENCE_KEY = "confidence"
        const val VERIFICATION_TIER_KEY = "tier"
        const val VERIFICATION_GUID_KEY = "guid"
        const val EXIT_REASON = "reason"
        const val EXIT_EXTRA = "extra"
    }

    override val presenter: CommCareContract.Presenter by inject { parametersOf(this, action) }

    override val guidSelectionNotifier: CommCareGuidSelectionNotifier by inject {
        parametersOf(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadClientApiKoinModules()
        CoroutineScope(Dispatchers.Main).launch {
            presenter.start()
        }
    }

    override fun returnRegistration(guid: String, skipCheck: Boolean) = Intent().let {
        val data = Bundle().apply {
            putString(SKIP_CHECK_KEY, skipCheck.toString())
            putString(REGISTRATION_GUID_KEY, guid)
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    override fun returnVerification(confidence: Int, tier: Tier, guid: String, skipCheck: Boolean) = Intent().let {
        val data = Bundle().apply {
            putString(SKIP_CHECK_KEY, skipCheck.toString())
            putInt(VERIFICATION_CONFIDENCE_KEY, confidence)
            putString(VERIFICATION_TIER_KEY, tier.name)
            putString(VERIFICATION_GUID_KEY, guid)
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    override fun returnExitForms(reason: String, extra: String, skipCheck: Boolean) = Intent().let {
        val data = Bundle().apply {
            putString(SKIP_CHECK_KEY, skipCheck.toString())
            putString(EXIT_REASON, reason)
            putString(EXIT_EXTRA, extra)
        }

        injectDataAsCommCareBundleIntoIntent(it, data)
        sendOkResult(it)
    }

    override fun returnErrorToClient(errorResponse: ErrorResponse, skipCheck: Boolean) = Intent().let {
        val data = Bundle().apply {
            putString(SKIP_CHECK_KEY, skipCheck.toString())
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
    }

    override fun injectSessionIdIntoIntent(sessionId: String) {
        intent.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadClientApiKoinModules()
    }

}
