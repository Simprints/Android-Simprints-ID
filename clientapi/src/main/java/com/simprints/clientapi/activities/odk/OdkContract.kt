package com.simprints.clientapi.activities.odk

import android.content.Intent
import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Verification


interface OdkContract {

    interface View : BaseView<Presenter> {

        fun returnActionErrorToClient()

        fun requestRegisterCallout()

        fun requestIdentifyCallout()

        fun requestVerifyCallout()

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
