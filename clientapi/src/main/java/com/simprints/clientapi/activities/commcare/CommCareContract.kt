package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.baserequest.RequestContract
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Tier


interface CommCareContract {

    interface View : BaseView<Presenter>, RequestContract.RequestView {

        fun returnRegistration(guid: String, sessionId: String, flowCompletedCheck: Boolean, eventsJson: String?)

        fun returnIdentification(identifications: ArrayList<Identification>, sessionId: String)

        fun returnVerification(confidence: Int, tier: Tier, guid: String, sessionId: String, flowCompletedCheck: Boolean)

        fun returnExitForms(reason: String, extra: String, sessionId: String, flowCompletedCheck: Boolean)

        fun returnConfirmation(flowCompletedCheck: Boolean, sessionId: String)

        fun injectSessionIdIntoIntent(sessionId: String)

        fun returnErrorToClient(errorResponse: ErrorResponse, flowCompletedCheck: Boolean, sessionId: String, eventsJson: String?)
    }

    interface Presenter : BasePresenter, RequestContract.Presenter {
        override suspend fun start()
    }
}
