package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.simprintsrequests.responses.EnrollResponse
import com.simprints.clientapi.simprintsrequests.responses.IdentificationResponse
import com.simprints.clientapi.simprintsrequests.responses.RefusalFormResponse
import com.simprints.clientapi.simprintsrequests.responses.VerifyResponse
import com.simprints.libsimprints.Constants.*


class LibSimprintsPresenter(val view: LibSimprintsContract.View, val action: String?)
    : RequestPresenter(view), LibSimprintsContract.Presenter {

    override fun handleEnrollResponse(enroll: EnrollResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleIdentifyResponse(identify: IdentificationResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleVerifyResponse(verify: VerifyResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleResponseError() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun start() = when (action) {
        SIMPRINTS_REGISTER_INTENT -> processEnrollRequest()
        SIMPRINTS_IDENTIFY_INTENT -> processIdentifyRequest()
        SIMPRINTS_VERIFY_INTENT -> processVerifyRequest()
        SIMPRINTS_SELECT_GUID_INTENT -> processConfirmIdentifyRequest()
        else -> view.returnIntentActionErrorToClient()
    }


}

