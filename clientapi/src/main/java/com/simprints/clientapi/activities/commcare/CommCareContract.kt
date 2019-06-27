package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.baserequest.RequestContract
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier


interface CommCareContract {

    interface View : BaseView<Presenter>, RequestContract.RequestView {

        fun returnRegistration(guid: String, skipCheck: Boolean)

        fun returnIdentification(identifications: ArrayList<Identification>, sessionId: String)

        fun returnVerification(confidence: Int, tier: Tier, guid: String, skipCheck: Boolean)

        fun returnExitForms(reason: String, extra: String, skipCheck: Boolean)

        fun injectSessionIdIntoIntent(sessionId: String)

    }

    interface Presenter : BasePresenter, RequestContract.Presenter
}
