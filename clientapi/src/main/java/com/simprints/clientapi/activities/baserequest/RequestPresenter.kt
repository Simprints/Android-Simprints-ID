package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.clientrequests.builders.*
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.clientrequests.validators.VerifyValidator


abstract class RequestPresenter(private val view: RequestContract.RequestView)
    : RequestContract.Presenter {

    override fun processEnrollRequest() = validateAndSendRequest(
        EnrollBuilder(view.enrollExtractor, EnrollValidator(view.enrollExtractor))
    )

    override fun processIdentifyRequest() = validateAndSendRequest(
        IdentifyBuilder(view.identifyExtractor, IdentifyValidator(view.identifyExtractor))
    )

    override fun processVerifyRequest() = validateAndSendRequest(
        VerifyBuilder(view.verifyExtractor, VerifyValidator(view.verifyExtractor))
    )

    override fun processConfirmIdentifyRequest() = validateAndSendRequest(ConfirmIdentifyBuilder(
        view.confirmIdentifyExtractor, ConfirmIdentifyValidator(view.confirmIdentifyExtractor)
    ))

    override fun validateAndSendRequest(builder: ClientRequestBuilder) = try {
        val request = builder.build()
        view.sendSimprintsRequest(request)
    } catch (exception: Exception) {
        view.handleClientRequestError(exception)
    }

}
