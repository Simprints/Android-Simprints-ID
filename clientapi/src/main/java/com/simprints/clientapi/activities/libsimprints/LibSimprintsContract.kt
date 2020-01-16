package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.baserequest.RequestContract
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Verification


interface LibSimprintsContract {

    interface View : BaseView<Presenter>, RequestContract.RequestView {

        fun returnRegistration(registration: Registration, sessionId: String, flowCompletedCheck: Boolean)

        fun returnIdentification(identifications: ArrayList<Identification>, sessionId: String, flowCompletedCheck: Boolean)

        fun returnVerification(verification: Verification, sessionId: String, flowCompletedCheck: Boolean)

        fun returnRefusalForms(refusalForm: RefusalForm, sessionId: String, flowCompletedCheck: Boolean)

        fun returnConfirmation(identificationOutcome: Boolean)

    }

    interface Presenter : BasePresenter, RequestContract.Presenter {
        override suspend fun start()
    }
}
