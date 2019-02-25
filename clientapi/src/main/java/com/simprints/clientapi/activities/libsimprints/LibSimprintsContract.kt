package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.baserequest.RequestContract
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Verification


interface LibSimprintsContract {

    interface View : BaseView<Presenter>, RequestContract.RequestView {

        fun returnRegistration(registration: Registration)

        fun returnIdentification(identifications: ArrayList<Identification>, sessionId: String)

        fun returnVerification(verification: Verification)

    }

    interface Presenter : BasePresenter, RequestContract.Presenter

}
