package com.simprints.clientapi.activities.odk

import com.simprints.clientapi.clientrequests.builders.EnrollmentBuilder
import com.simprints.clientapi.clientrequests.requests.ClientEnrollmentRequest
import com.simprints.clientapi.clientrequests.validators.EnrollmentValidator
import com.simprints.clientapi.extensions.getConfidencesString
import com.simprints.clientapi.extensions.getIdsString
import com.simprints.clientapi.extensions.getTiersString
import com.simprints.clientapi.simprintsrequests.EnrollmentRequest
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
        ACTION_IDENTIFY -> view.requestIdentifyCallout()
        ACTION_VERIFY -> view.requestVerifyCallout()
        ACTION_CONFIRM_IDENTITY -> view.requestConfirmIdentityCallout()
        else -> view.returnActionErrorToClient()
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

    override fun processReturnError() = view.returnActionErrorToClient()

    private fun handleEnrollmentRequest() {
        EnrollmentBuilder(view.enrollmentExtractor, EnrollmentValidator(view.enrollmentExtractor)).let {
            try {
                val request = it.build() as ClientEnrollmentRequest
                view.requestRegisterCallout(EnrollmentRequest(request))
            } catch (exception: Exception) {
                view.showErrorForException(exception)
            }
        }
    }

}
