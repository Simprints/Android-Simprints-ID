package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.baserequest.RequestContract
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier


interface CommCareContract {

    interface View : BaseView<Presenter>, RequestContract.RequestView {

        fun returnRegistration(registration: Registration)

        fun returnIdentification(identifications: ArrayList<Identification>, sessionId: String)

        fun returnVerification(confidence: Int, tier: Tier, guid: String)

        fun returnRefusalForms(refusalForm: RefusalForm)

        fun injectSessionIdIntoIntent(sessionId: String)

    }

    interface Presenter : BasePresenter, RequestContract.Presenter {

        fun isAnErrorToSkipCheck(errorResponse: ErrorResponse): Boolean

    }
}
