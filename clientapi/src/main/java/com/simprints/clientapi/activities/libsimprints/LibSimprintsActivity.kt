package com.simprints.clientapi.activities.libsimprints

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.Companion.buildLibSimprintsAction
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction.ConfirmIdentity
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.identity.DefaultGuidSelectionNotifier
import com.simprints.libsimprints.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class LibSimprintsActivity : RequestActivity(), LibSimprintsContract.View {

    private val action: LibSimprintsAction
        get() = buildLibSimprintsAction(intent.action)

    override val presenter: LibSimprintsContract.Presenter by inject { parametersOf(this, action) }

    override val guidSelectionNotifier = DefaultGuidSelectionNotifier(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (action != ConfirmIdentity)
            showLauncherScreen()

        loadClientApiKoinModules()
    }

    override fun returnRegistration(registration: Registration,
                                    sessionId: String,
                                    flowCompletedCheck: Boolean) = Intent().let {

        it.putExtra(Constants.SIMPRINTS_REGISTRATION, registration)
        it.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        it.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, flowCompletedCheck)
        sendOkResult(it)
    }

    override fun returnIdentification(identifications: ArrayList<Identification>,
                                      sessionId: String,
                                      flowCompletedCheck: Boolean) = Intent().let {

        it.putParcelableArrayListExtra(Constants.SIMPRINTS_IDENTIFICATIONS, identifications)
        it.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        it.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, flowCompletedCheck)
        sendOkResult(it)
    }

    override fun returnVerification(verification: Verification,
                                    sessionId: String,
                                    flowCompletedCheck: Boolean) = Intent().let {
        it.putExtra(Constants.SIMPRINTS_VERIFICATION, verification)
        it.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        it.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, flowCompletedCheck)
        sendOkResult(it)
    }

    override fun returnRefusalForms(refusalForm: RefusalForm,
                                    sessionId: String,
                                    flowCompletedCheck: Boolean) = Intent().let {

        it.putExtra(Constants.SIMPRINTS_REFUSAL_FORM, refusalForm)
        it.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        it.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, flowCompletedCheck)
        sendOkResult(it)
    }

    override fun returnConfirmation(identificationOutcome: Boolean, sessionId: String) = Intent().let {
        it.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, identificationOutcome)
        it.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
        sendOkResult(it)
    }

    override fun returnErrorToClient(errorResponse: ErrorResponse,
                                     flowCompletedCheck: Boolean,
                                     sessionId: String) {
        setResult(
            errorResponse.reason.libSimprintsResultCode(),
            Intent().let {
                it.putExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, flowCompletedCheck)
                it.putExtra(Constants.SIMPRINTS_SESSION_ID, sessionId)
            }
        )
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadClientApiKoinModules()
    }

}
