package com.simprints.clientapi.activities.libsimprints

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.activities.libsimprints.di.LibSimprintsComponentInjector
import com.simprints.clientapi.domain.requests.IntegrationInfo
import com.simprints.libsimprints.*
import javax.inject.Inject


class LibSimprintsActivity : RequestActivity(), LibSimprintsContract.View {

    override val integrationInfo = IntegrationInfo.STANDARD

    @Inject override lateinit var presenter: LibSimprintsContract.Presenter

    override val action: String?
        get() = intent.action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LibSimprintsComponentInjector.inject(this)

        presenter.start()
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

    override fun onDestroy() {
        super.onDestroy()
        LibSimprintsComponentInjector.setComponent(null)
    }
}
