package com.simprints.clientapi.activities.libsimprints

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.di.KoinInjector.Companion.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.Companion.unloadClientApiKoinModules
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.libsimprints.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class LibSimprintsActivity : RequestActivity(), LibSimprintsContract.View {

    override val presenter: LibSimprintsContract.Presenter by inject { parametersOf(this, action) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadClientApiKoinModules()
        CoroutineScope(Dispatchers.Main).launch { presenter.start() }
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

    override fun returnErrorToClient(errorResponse: ErrorResponse) {
        val resultCode = presenter.errorResponseReasonToResultCode[errorResponse.reason]
        setResult(resultCode ?: Activity.RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadClientApiKoinModules()
    }

}
