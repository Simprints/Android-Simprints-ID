package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.clientrequests.builders.*
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.extensions.getConfidencesString
import com.simprints.clientapi.extensions.getIdsString
import com.simprints.clientapi.extensions.getTiersString
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.Verification


class OdkPresenter(private val view: OdkContract.View,
                   private val action: String?) : OdkContract.Presenter {

    companion object {
        private const val PACKAGE_NAME = "com.simprints.simodkadapter"
        const val ACTION_REGISTER = "$PACKAGE_NAME.REGISTER"
        const val ACTION_IDENTIFY = "$PACKAGE_NAME.IDENTIFY"
        const val ACTION_VERIFY = "$PACKAGE_NAME.VERIFY"
        const val ACTION_CONFIRM_IDENTITY = "$PACKAGE_NAME.CONFIRM_IDENTITY"
    }

    override fun start() = when (action) {
        ACTION_REGISTER -> handleEnrollmentRequest()
        ACTION_IDENTIFY -> handleIdentifyRequest()
        ACTION_VERIFY -> handleVerifyRequest()
        ACTION_CONFIRM_IDENTITY -> handleConfirmIdentifyRequest()
        else -> view.returnIntentActionErrorToClient()
    }

    override fun processRegistration(registration: Registration) =
        view.returnRegistration(registration.guid)

    override fun processIdentification(identifications: ArrayList<Identification>, sessionId: String) =
        view.returnIdentification(
            identifications.getIdsString(),
            identifications.getConfidencesString(),
            identifications.getTiersString(),
            sessionId
        )

    override fun processVerification(verification: Verification) = view.returnVerification(
        verification.guid,
        verification.confidence.toString(),
        verification.tier.toString()
    )

    override fun processReturnError() = view.returnIntentActionErrorToClient()

    private fun handleEnrollmentRequest() = validateAndSendRequest(
        EnrollBuilder(view.enrollExtractor, EnrollValidator(view.enrollExtractor))
    )

    private fun handleVerifyRequest() = validateAndSendRequest(
        VerifyBuilder(view.verifyExtractor, VerifyValidator(view.verifyExtractor))
    )

    private fun handleIdentifyRequest() = validateAndSendRequest(
        IdentifyBuilder(view.identifyExtractor, IdentifyValidator(view.identifyExtractor))
    )

    private fun handleConfirmIdentifyRequest() = validateAndSendRequest(
        ConfirmIdentifyBuilder(view.confirmIdentifyExtractor,
            ConfirmIdentifyValidator(view.confirmIdentifyExtractor))
    )

    private fun validateAndSendRequest(builder: ClientRequestBuilder) = try {
        val request = builder.build()
        view.sendSimprintsRequest(request)
    } catch (exception: Exception) {
        view.handleClientRequestError(exception)
    }

}
