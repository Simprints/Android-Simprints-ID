package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.baserequest.RequestContract
import com.simprints.clientapi.domain.responses.entities.MatchConfidence

interface OdkContract {

    interface View : BaseView<Presenter>, RequestContract.RequestView {

        fun returnRegistration(registrationId: String, sessionId: String, flowCompletedCheck:Boolean)

        fun returnIdentification(idList: String, confidenceScoresList: String,
                                 tierList: String, sessionId: String, matchConfidencesList: String, flowCompletedCheck:Boolean)

        fun returnVerification(id: String, confidence: String, tier: String, sessionId: String, flowCompletedCheck:Boolean)

        fun returnExitForm(reason: String, extra: String, sessionId: String, flowCompletedCheck:Boolean)

        fun returnConfirmation(flowCompletedCheck: Boolean, sessionId: String)

    }

    interface Presenter : BasePresenter, RequestContract.Presenter {
        override suspend fun start()
    }
}
