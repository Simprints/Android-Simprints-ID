package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.activities.baserequest.RequestPresenter
import com.simprints.clientapi.extensions.getConfidencesString
import com.simprints.clientapi.extensions.getIdsString
import com.simprints.clientapi.extensions.getTiersString
import com.simprints.clientapi.models.domain.responses.EnrollResponse
import com.simprints.clientapi.models.domain.responses.IdentifyResponse
import com.simprints.clientapi.models.domain.responses.RefusalFormResponse
import com.simprints.clientapi.models.domain.responses.VerifyResponse


class OdkPresenter(val view: OdkContract.View,
                   private val action: String?) : RequestPresenter(view), OdkContract.Presenter {

    companion object {
        private const val PACKAGE_NAME = "com.simprints.simodkadapter"
        const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
        const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"
    }

    override fun start() = when (action) {
        ACTION_REGISTER -> processEnrollRequest()
        ACTION_IDENTIFY -> processIdentifyRequest()
        ACTION_VERIFY -> processVerifyRequest()
        ACTION_CONFIRM_IDENTITY -> processConfirmIdentifyRequest()
        else -> view.returnIntentActionErrorToClient()
    }

    override fun handleEnrollResponse(enroll: EnrollResponse) = view.returnRegistration(enroll.guid)

    override fun handleIdentifyResponse(identify: IdentifyResponse) = view.returnIdentification(
        identify.identifications.getIdsString(),
        identify.identifications.getConfidencesString(),
        identify.identifications.getTiersString(),
        identify.sessionId
    )

    override fun handleVerifyResponse(verify: VerifyResponse) = view.returnVerification(
        verify.guid,
        verify.confidence.toString(),
        verify.tier.toString()
    )

    override fun handleRefusalResponse(refusalForm: RefusalFormResponse) =
        view.returnRefusalForm(refusalForm.reason, refusalForm.extra)

    override fun handleResponseError() = view.returnIntentActionErrorToClient()

}
