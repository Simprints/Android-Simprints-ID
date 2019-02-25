package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.simprintsrequests.responses.ClientApiEnrollResponse
import com.simprints.clientapi.simprintsrequests.responses.ClientApiIdentifyResponse
import com.simprints.clientapi.simprintsrequests.responses.ClientApiRefusalFormResponse
import com.simprints.clientapi.simprintsrequests.responses.ClientApiVerifyResponse
import com.simprints.libsimprints.*
import com.simprints.libsimprints.Constants.*


class LibSimprintsPresenter(val view: LibSimprintsContract.View, val action: String?)
    : RequestPresenter(view), LibSimprintsContract.Presenter {

    override fun start() = when (action) {
        SIMPRINTS_REGISTER_INTENT -> processEnrollRequest()
        SIMPRINTS_IDENTIFY_INTENT -> processIdentifyRequest()
        SIMPRINTS_VERIFY_INTENT -> processVerifyRequest()
        SIMPRINTS_SELECT_GUID_INTENT -> processConfirmIdentifyRequest()
        else -> view.returnIntentActionErrorToClient()
    }

    override fun handleEnrollResponse(enroll: ClientApiEnrollResponse) =
        view.returnRegistration(Registration(enroll.guid))

    override fun handleIdentifyResponse(identify: ClientApiIdentifyResponse) =
        view.returnIdentification(ArrayList(identify.identifications.map {
            Identification(it.guid, it.confidence, Tier.valueOf(it.tier.name))
        }), identify.sessionId)

    override fun handleVerifyResponse(verify: ClientApiVerifyResponse) = view.returnVerification(
        Verification(verify.confidence, Tier.valueOf(verify.tier.name), verify.guid)
    )

    override fun handleRefusalResponse(refusalForm: ClientApiRefusalFormResponse) =
        view.returnRefusalForms(RefusalForm(refusalForm.reason, refusalForm.extra))

    override fun handleResponseError() = view.returnIntentActionErrorToClient()

}

