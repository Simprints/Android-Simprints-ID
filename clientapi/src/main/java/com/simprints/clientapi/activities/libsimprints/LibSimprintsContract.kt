package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.baserequest.RequestContract
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Verification


interface LibSimprintsContract {

    interface View : BaseView<Presenter>, RequestContract.RequestView {

        fun returnRegistration(
            registration: Registration,
            sessionId: String,
            flowCompletedCheck: Boolean,
            eventsJson: String?,
            subjectActions: String?
        )

        fun returnIdentification(
            identifications: ArrayList<Identification>,
            sessionId: String,
            flowCompletedCheck: Boolean,
            eventsJson: String?
        )

        fun returnVerification(
            verification: Verification,
            sessionId: String,
            flowCompletedCheck: Boolean,
            eventsJson: String?
        )

        fun returnRefusalForms(
            refusalForm: RefusalForm,
            sessionId: String,
            flowCompletedCheck: Boolean,
            eventsJson: String?
        )

        fun returnConfirmation(
            identificationOutcome: Boolean,
            sessionId: String,
            eventsJson: String?
        )

        fun returnErrorToClient(
            errorResponse: ErrorResponse,
            flowCompletedCheck: Boolean,
            sessionId: String,
            eventsJson: String?
        )

    }

    interface Presenter : BasePresenter, RequestContract.Presenter {
        override suspend fun start()
    }
}
