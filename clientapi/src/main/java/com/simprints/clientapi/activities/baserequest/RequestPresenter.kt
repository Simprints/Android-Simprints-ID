package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.clientrequests.builders.*
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.InvalidIntentEvent
import com.simprints.clientapi.domain.confirmations.BaseConfirmation
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.exceptions.InvalidClientRequestException
import com.simprints.clientapi.exceptions.InvalidRequestException
import javax.inject.Inject


abstract class RequestPresenter(private val view: RequestContract.RequestView)
    : RequestContract.Presenter {

    @Inject lateinit var clientApiSessionEventsManager: ClientApiSessionEventsManager

    init {
        determineIfIntentIsSuspiciousAndStoreInSessions()
    }

    private fun determineIfIntentIsSuspiciousAndStoreInSessions() {

    }

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
        when (request) {
            is BaseRequest -> view.sendSimprintsRequest(request)
            is BaseConfirmation -> view.sendSimprintsConfirmationAndFinish(request)
            else -> throw InvalidClientRequestException()
        }
    } catch (exception: InvalidRequestException) {
        addInvalidSessionInBackground().also { view.handleClientRequestError(exception) }
    }

    private fun addInvalidSessionInBackground() {
       clientApiSessionEventsManager
           .addInvalidSession(InvalidIntentEvent(view.getIntentAction(), view.getIntentExtras()))
    }

}
