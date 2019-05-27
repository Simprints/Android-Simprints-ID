package com.simprints.clientapi.activities.commcare

import android.os.Bundle
import android.os.PersistableBundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsContract
import com.simprints.clientapi.di.koinModule
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.parameter.parametersOf


class CommCareActivity : RequestActivity(), LibSimprintsContract.View {

    override val presenter: LibSimprintsContract.Presenter by inject { parametersOf(this, action) }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        loadKoinModules(koinModule)

        presenter.start()
    }

    override fun returnRegistration(registration: Registration) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun returnIdentification(identifications: ArrayList<Identification>, sessionId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun returnVerification(confidence: Int, tier: Tier, guid: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun returnRefusalForms(refusalForm: RefusalForm) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
