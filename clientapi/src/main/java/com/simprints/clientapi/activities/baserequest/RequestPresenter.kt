package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.activities.errors.ClientApiAlert.*
import com.simprints.clientapi.clientrequests.builders.*
import com.simprints.clientapi.clientrequests.validators.*
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.exceptions.*
import com.simprints.clientapi.tools.DeviceManager
import com.simprints.logging.Simber

abstract class RequestPresenter(private val view: RequestContract.RequestView,
                                private val eventsManager: ClientApiSessionEventsManager,
                                private val deviceManager: DeviceManager,
                                protected val crashReportManager: ClientApiCrashReportManager)
    : RequestContract.Presenter {

    override suspend fun processEnrolRequest() = validateAndSendRequest(
        EnrolBuilder(view.enrolExtractor, EnrolValidator(view.enrolExtractor))
    )

    override suspend fun processIdentifyRequest() = validateAndSendRequest(
        IdentifyBuilder(view.identifyExtractor, IdentifyValidator(view.identifyExtractor))
    )

    override suspend fun processVerifyRequest() = validateAndSendRequest(
        VerifyBuilder(view.verifyExtractor, VerifyValidator(view.verifyExtractor))
    )

    override suspend fun processConfirmIdentityRequest() = validateAndSendRequest(
        ConfirmIdentifyBuilder(
            view.confirmIdentityExtractor,
            ConfirmIdentityValidator(view.confirmIdentityExtractor)
        )
    )

    override suspend fun processEnrolLastBiometrics() = validateAndSendRequest(
        EnrolLastBiometricsBuilder(
            view.enrolLastBiometricsExtractor,
            EnrolLastBiometricsValidator(view.enrolLastBiometricsExtractor, eventsManager.getCurrentSessionId(), eventsManager.isCurrentSessionAnIdentificationOrEnrolment())
        )
    )

    override suspend fun validateAndSendRequest(builder: ClientRequestBuilder) = try {
        val request = builder.build()
        addSuspiciousEventIfRequired(request)
        view.sendSimprintsRequest(request)
    } catch (exception: InvalidRequestException) {
        Simber.d(exception)
        logInvalidSessionInBackground()
        handleInvalidRequest(exception)
    }

    protected suspend fun runIfDeviceIsNotRooted(block: suspend () -> Unit) {
        try {
            deviceManager.checkIfDeviceIsRooted()
            block()
        } catch (ex: RootedDeviceException) {
            handleRootedDevice(ex)
        }
    }

    private fun handleRootedDevice(exception: RootedDeviceException) {
        crashReportManager.logExceptionOrSafeException(exception)
        view.handleClientRequestError(ROOTED_DEVICE)
    }

    private fun handleInvalidRequest(exception: InvalidRequestException) {
        when (exception) {
            is InvalidMetadataException -> INVALID_METADATA
            is InvalidModuleIdException -> INVALID_MODULE_ID
            is InvalidProjectIdException -> INVALID_PROJECT_ID
            is InvalidSelectedIdException -> INVALID_SELECTED_ID
            is InvalidSessionIdException -> INVALID_SESSION_ID
            is InvalidUserIdException -> INVALID_USER_ID
            is InvalidVerifyIdException -> INVALID_VERIFY_ID
            is InvalidStateForIntentAction -> INVALID_STATE_FOR_INTENT_ACTION
        }.also {
            view.handleClientRequestError(it)
        }
    }

    private suspend fun addSuspiciousEventIfRequired(request: BaseRequest) {
        if (request.unknownExtras.isNotEmpty()) {
            eventsManager.addSuspiciousIntentEvent(request.unknownExtras)
        }
    }

    private suspend fun logInvalidSessionInBackground() {
        eventsManager.addInvalidIntentEvent(view.intentAction ?: "", view.extras ?: emptyMap())
    }

}
