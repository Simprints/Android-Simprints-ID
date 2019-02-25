package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.baserequest.RequestContract


interface OdkContract {

    interface View : BaseView<Presenter>, RequestContract.RequestView {

        fun returnRegistration(registrationId: String)

        fun returnIdentification(idList: String, confidenceList: String, tierList: String, sessionId: String)

        fun returnVerification(id: String, confidence: String, tier: String)

        fun returnRefusalForm(reason: String, extra: String)

    }

    interface Presenter : BasePresenter, RequestContract.Presenter

}
