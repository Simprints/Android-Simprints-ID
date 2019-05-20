package com.simprints.clientapi.activities.baserequest

import com.google.gson.reflect.TypeToken
import com.simprints.clientapi.clientrequests.builders.*
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.InvalidIntentEvent
import com.simprints.clientapi.controllers.core.eventData.model.SuspiciousIntentEvent
import com.simprints.clientapi.domain.ClientBase
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.IntegrationInfo
import com.simprints.clientapi.domain.requests.confirmations.BaseConfirmation
import com.simprints.clientapi.exceptions.InvalidClientRequestException
import com.simprints.clientapi.exceptions.InvalidRequestException
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.json.GsonBuilder


abstract class RequestPresenter constructor(private val view: RequestContract.RequestView,
                                            private val timeHelper: ClientApiTimeHelper,
                                            private var clientApiSessionEventsManager: ClientApiSessionEventsManager,
                                            private val clientApiCrashReportManager: ClientApiCrashReportManager,
                                            private var gsonBuilder: GsonBuilder,
                                            private val integrationInfo: IntegrationInfo)
    : RequestContract.Presenter {

    override fun processEnrollRequest() = validateAndSendRequest(
        EnrollBuilder(view.enrollExtractor, EnrollValidator(view.enrollExtractor), integrationInfo)
    )

    override fun processIdentifyRequest() = validateAndSendRequest(
        IdentifyBuilder(view.identifyExtractor, IdentifyValidator(view.identifyExtractor), integrationInfo)
    )

    override fun processVerifyRequest() = validateAndSendRequest(
        VerifyBuilder(view.verifyExtractor, VerifyValidator(view.verifyExtractor), integrationInfo)
    )

    override fun processConfirmIdentifyRequest() = validateAndSendRequest(ConfirmIdentifyBuilder(
        view.confirmIdentifyExtractor, ConfirmIdentifyValidator(view.confirmIdentifyExtractor), integrationInfo
    ))

    override fun validateAndSendRequest(builder: ClientRequestBuilder) = try {
        val request = builder.build()
        addSuspiciousEventIfRequired(request)
        when (request) {
            is BaseRequest -> view.sendSimprintsRequest(request)
            is BaseConfirmation -> view.sendSimprintsConfirmationAndFinish(request)
            else -> throw InvalidClientRequestException()
        }
    } catch (exception: InvalidRequestException) {
        addInvalidSessionInBackground().also { view.handleClientRequestError(exception) }
    }

    private fun addSuspiciousEventIfRequired(request: ClientBase) {
        try {
            val extrasKeys = extractExtraKeysAndValuesFromIntent(request)
            if (extrasKeys.isNotEmpty()) {
                clientApiSessionEventsManager
                    .addSessionEvent(SuspiciousIntentEvent(timeHelper.now(), extrasKeys))
            }
        } catch (t: Throwable) {
            clientApiCrashReportManager.logExceptionOrSafeException(t)
            t.printStackTrace()
        }
    }

    private fun extractExtraKeysAndValuesFromIntent(request: ClientBase): Map<String, Any?> =
        with(gsonBuilder.build()) {
            val requestJson = this.toJson(request)
            val expectedKeysAndValues = this.fromJson<Map<String, Any>>(requestJson, object : TypeToken<Map<String, Any>>() {}.type)
            val keysAndValuesFromIntent = extractKeysAndValuesFromIntent()
            keysAndValuesFromIntent.filterKeys { !expectedKeysAndValues.containsKey(it) }
        }

    private fun extractKeysAndValuesFromIntent() =
        view.getIntentExtras()?.filter { it.key.isNotEmpty() } ?: emptyMap()


    private fun addInvalidSessionInBackground() {
        clientApiSessionEventsManager
            .addSessionEvent(InvalidIntentEvent(
                timeHelper.now(),
                view.getIntentAction(),
                view.getIntentExtras() ?: emptyMap()))
    }
}
