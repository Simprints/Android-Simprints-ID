package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.baserequest.RequestContract

interface OdkContract {

    interface View : BaseView<Presenter>, RequestContract.RequestView {

        fun returnRegistration(registrationId: String, skipCheck:Boolean)

        fun returnIdentification(idList: String, confidenceList: String, tierList: String, sessionId: String, skipCheck:Boolean)

        fun returnVerification(id: String, confidence: String, tier: String, skipCheck:Boolean)

        fun returnExitForm(reason: String, extra: String, skipCheck:Boolean)

    }

    interface Presenter : BasePresenter, RequestContract.Presenter

}
