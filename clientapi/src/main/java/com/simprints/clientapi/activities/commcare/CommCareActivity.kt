package com.simprints.clientapi.activities.commcare

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.di.koinModule
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.libsimprints.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.parameter.parametersOf


class CommCareActivity : RequestActivity(), CommCareContract.View {

    companion object {
        const val COMMCARE_BUNDLE_KEY = "odk_intent_bundle"

        const val SKIP_CHECK_KEY = "skipCheck"
        const val REGISTRATION_GUID_KEY = "guid"
    }

    override val presenter: CommCareContract.Presenter by inject { parametersOf(this, action) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(koinModule)
        CoroutineScope(Dispatchers.Main).launch { presenter.start() }
    }


    override fun returnRegistration(registration: Registration) = Intent().let {
        val responseForCommCare = Bundle()
        responseForCommCare.putBoolean(SKIP_CHECK_KEY, true)
        responseForCommCare.putString(REGISTRATION_GUID_KEY, registration.guid)

        it.putExtra(COMMCARE_BUNDLE_KEY, responseForCommCare)
        sendOkResult(it)
    }

    override fun returnIdentification(identifications: ArrayList<Identification>,
                                      sessionId: String) = Intent().let {
        it.putParcelableArrayListExtra(Constants.SIMPRINTS_IDENTIFICATIONS, identifications)
        it.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        sendOkResult(it)
    }

    override fun returnVerification(confidence: Int, tier: Tier, guid: String) = Intent().let {
        it.putExtra(Constants.SIMPRINTS_VERIFICATION, Verification(confidence, tier, guid))
        sendOkResult(it)
    }

    override fun returnRefusalForms(refusalForm: RefusalForm) = Intent().let {
        it.putExtra(Constants.SIMPRINTS_REFUSAL_FORM, refusalForm)
        sendOkResult(it)
    }

    override fun returnErrorToClient(errorResponse: ErrorResponse) = Intent().let {
        it.putExtra(SKIP_CHECK_KEY, presenter.isAnErrorToSkipCheck(errorResponse))
        sendOkResult(it)
    }

    override fun injectSessionIdIntoIntent(sessionId: String) {
        intent.putExtra(com.simprints.id.domain.Constants.SIMPRINTS_SESSION_ID, sessionId)
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadKoinModules(koinModule)
    }

}
