package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.ClientRequestView
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Verification


interface OdkContract {

    interface View : BaseView<Presenter>, ClientRequestView {

        fun requestConfirmIdentityCallout()

        fun returnRegistration(registrationId: String)

        fun returnIdentification(idList: String, confidenceList: String, tierList: String, sessionId: String)

        fun returnVerification(id: String, confidence: String, tier: String)

    }

    interface Presenter : BasePresenter {

        fun processRegistration(registration: Registration)

        fun processIdentification(identifications: ArrayList<Identification>, sessionId: String)

        fun processVerification(verification: Verification)

        fun processReturnError()

    }

}
