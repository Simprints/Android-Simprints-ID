package com.simprints.clientapi.activities.commcare

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import com.simprints.clientapi.identity.CommCareGuidSelectionNotifier
import com.simprints.libsimprints.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class CommCareActivity : RequestActivity(), CommCareContract.View {

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

    override fun returnRegistration(registration: Registration) = Intent().let {
        it.putExtra(Constants.SIMPRINTS_REGISTRATION, registration)
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

    override fun injectSessionIdIntoIntent(sessionId: String) {
        intent.putExtra(com.simprints.id.domain.Constants.SIMPRINTS_SESSION_ID, sessionId)
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadClientApiKoinModules()
    }

}
