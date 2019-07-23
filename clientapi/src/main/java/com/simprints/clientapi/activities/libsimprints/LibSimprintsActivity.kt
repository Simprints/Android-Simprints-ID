package com.simprints.clientapi.activities.libsimprints

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.identity.DefaultGuidSelectionNotifier
import com.simprints.libsimprints.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class LibSimprintsActivity : RequestActivity(), LibSimprintsContract.View {

    override val presenter: LibSimprintsContract.Presenter by inject { parametersOf(this, action) }

    override val guidSelectionNotifier = DefaultGuidSelectionNotifier(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadClientApiKoinModules()
        CoroutineScope(Dispatchers.Main).launch { presenter.start() }
    }

    override fun returnRegistration(registration: Registration,
                                    skipCheck: Boolean) = Intent().let {

        it.putExtra(Constants.SIMPRINTS_REGISTRATION, registration)
        it.putExtra(Constants.SIMPRINTS_SKIP_CHECK, skipCheck)
        sendOkResult(it)
    }

    override fun returnIdentification(identifications: ArrayList<Identification>,
                                      sessionId: String,
                                      skipCheck: Boolean) = Intent().let {

        it.putParcelableArrayListExtra(Constants.SIMPRINTS_IDENTIFICATIONS, identifications)
        it.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        it.putExtra(Constants.SIMPRINTS_SKIP_CHECK, skipCheck)
        sendOkResult(it)
    }

    override fun returnVerification(verification: Verification,
                                    skipCheck: Boolean) = Intent().let {
        it.putExtra(Constants.SIMPRINTS_VERIFICATION, verification)
        it.putExtra(Constants.SIMPRINTS_SKIP_CHECK, skipCheck)
        sendOkResult(it)
    }

    override fun returnRefusalForms(refusalForm: RefusalForm,
                                    skipCheck: Boolean) = Intent().let {

        it.putExtra(Constants.SIMPRINTS_REFUSAL_FORM, refusalForm)
        it.putExtra(Constants.SIMPRINTS_SKIP_CHECK, skipCheck)
        sendOkResult(it)
    }

    override fun returnErrorToClient(errorResponse: ErrorResponse, skipCheck: Boolean) {
        setResult(
            errorResponse.reason.libSimprintsResultCode(),
            Intent().putExtra(Constants.SIMPRINTS_SKIP_CHECK, skipCheck))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadClientApiKoinModules()
    }

}
