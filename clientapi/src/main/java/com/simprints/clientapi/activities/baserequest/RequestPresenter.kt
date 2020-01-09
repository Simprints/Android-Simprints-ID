package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.activities.errors.ClientApiAlert.*
import com.simprints.clientapi.clientrequests.builders.*
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentityValidator
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.domain.ClientBase
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.confirmations.BaseConfirmation
import com.simprints.clientapi.exceptions.*
import com.simprints.clientapi.extensions.doInBackground
import com.simprints.clientapi.tools.DeviceManager

abstract class RequestPresenter(private val view: RequestContract.RequestView,
                                private var eventsManager: ClientApiSessionEventsManager,
                                private val deviceManager: DeviceManager)
    : RequestContract.Presenter {

    override suspend fun start() {
        if (deviceManager.isDeviceRooted()) {
            view.handleRootedDevice()
            return
        }
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

    override fun processConfirmIdentityRequest() = validateAndSendRequest(
        ConfirmIdentifyBuilder(
            view.confirmIdentityExtractor,
            ConfirmIdentityValidator(view.confirmIdentityExtractor)
        )
    )

    override fun validateAndSendRequest(builder: ClientRequestBuilder) = try {
        val request = builder.build()
        addSuspiciousEventIfRequired(request)

        when (request) {
            is BaseRequest -> view.sendSimprintsRequest(request)
            is BaseConfirmation -> view.sendSimprintsConfirmation(request)
            else -> throw InvalidClientRequestException()
        }
    } catch (exception: InvalidRequestException) {
        exception.printStackTrace()
        logInvalidSessionInBackground()
        handleInvalidRequest(exception)
    }

    private fun handleInvalidRequest(exception: InvalidRequestException) {
        when (exception) {
            is InvalidClientRequestException -> INVALID_CLIENT_REQUEST
            is InvalidMetadataException -> INVALID_METADATA
            is InvalidModuleIdException -> INVALID_MODULE_ID
            is InvalidProjectIdException -> INVALID_PROJECT_ID
            is InvalidSelectedIdException -> INVALID_SELECTED_ID
            is InvalidSessionIdException -> INVALID_SESSION_ID
            is InvalidUserIdException -> INVALID_USER_ID
            is InvalidVerifyIdException -> INVALID_VERIFY_ID
        }.also {
            view.handleClientRequestError(it)
        }
    }

    private fun addSuspiciousEventIfRequired(request: ClientBase) {
        if (request.unknownExtras.isNotEmpty()) {
            eventsManager.addSuspiciousIntentEvent(request.unknownExtras).doInBackground()
        }
    }

    private fun logInvalidSessionInBackground() {
        eventsManager.addInvalidIntentEvent(view.action ?: "", view.extras ?: emptyMap())
            .doInBackground()
    }
}
