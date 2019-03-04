package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.domain.responses.EnrollResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.RefusalFormResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.libsimprints.Constants.*
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Tier


class LibSimprintsPresenter(val view: LibSimprintsContract.View, val action: String?)
    : RequestPresenter(view), LibSimprintsContract.Presenter {

    override fun start() = when (action) {
        SIMPRINTS_REGISTER_INTENT -> processEnrollRequest()
        SIMPRINTS_IDENTIFY_INTENT -> processIdentifyRequest()
        SIMPRINTS_VERIFY_INTENT -> processVerifyRequest()
        SIMPRINTS_SELECT_GUID_INTENT -> processConfirmIdentifyRequest()
        else -> view.returnIntentActionErrorToClient()
    }

    override fun handleEnrollResponse(enroll: EnrollResponse) =
        view.returnRegistration(Registration(enroll.guid))

    override fun handleIdentifyResponse(identify: IdentifyResponse) =
        view.returnIdentification(ArrayList(identify.identifications.map {
            Identification(it.guid, it.confidence, Tier.valueOf(it.tier.name))
        }), identify.sessionId)

    override fun handleVerifyResponse(verify: VerifyResponse) = view.returnVerification(
        verify.confidence, Tier.valueOf(verify.tier.name), verify.guid
    )

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) =
        view.returnRefusalForms(RefusalForm(refusalForm.reason, refusalForm.extra))

    override fun handleResponseError() = view.returnIntentActionErrorToClient()

}

