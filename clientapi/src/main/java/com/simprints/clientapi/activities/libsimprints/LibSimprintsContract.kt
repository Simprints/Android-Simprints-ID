package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.baserequest.RequestContract
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier


interface LibSimprintsContract {

    interface View : BaseView<Presenter>, RequestContract.RequestView {

        fun returnRegistration(registration: Registration)

        fun returnIdentification(identifications: ArrayList<Identification>, sessionId: String)

        fun returnVerification(confidence: Int, tier: Tier, guid: String)

        fun returnRefusalForms(refusalForm: RefusalForm)

    }

    interface Presenter : BasePresenter, RequestContract.Presenter

}
