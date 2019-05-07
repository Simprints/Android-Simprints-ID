package com.simprints.clientapi.activities.baserequest

import com.google.gson.reflect.TypeToken
import com.simprints.clientapi.clientrequests.builders.*
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentifyValidator
import com.simprints.clientapi.clientrequests.validators.EnrollValidator
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.InvalidIntentEvent
import com.simprints.clientapi.controllers.core.eventData.model.SuspiciousIntentEvent
import com.simprints.clientapi.domain.ClientBase
import com.simprints.clientapi.domain.confirmations.BaseConfirmation
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.exceptions.InvalidClientRequestException
import com.simprints.clientapi.exceptions.InvalidRequestException
import com.simprints.clientapi.tools.json.GsonBuilder


abstract class RequestPresenter constructor(private val view: RequestContract.RequestView,
                                            private var clientApiSessionEventsManager: ClientApiSessionEventsManager,
                                            private var gsonBuilder: GsonBuilder)
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
        addSuspiciousEventIfRequired(request)
        when (request) {
            is BaseRequest -> view.sendSimprintsRequest(request)
            is BaseConfirmation -> view.sendSimprintsConfirmationAndFinish(request)
            else -> throw InvalidClientRequestException()
        }
    } catch (exception: InvalidRequestException) {
        addInvalidSessionInBackground().also { view.handleClientRequestError(exception) }
    }

    protected fun addSuspiciousEventIfRequired(request: ClientBase) {
        with(gsonBuilder.build()) {
            val extrasKeys = extractExtraKeysFromIntent(request)
            if (extrasKeys.isNotEmpty()) {
                clientApiSessionEventsManager
                    .addSessionEvent(SuspiciousIntentEvent(this.toJson(extrasKeys)))
            }
        }
    }

    private fun extractExtraKeysFromIntent(request: ClientBase): Set<String> =
        with(gsonBuilder.build()) {
            val requestJson = this.toJson(request)
            val rightKeysAndValues = this.fromJson<Map<String, String>>(requestJson, object : TypeToken<Map<String, String>>() {}.type)
            val keysAndValuesInIntent = extractKeysFromIntent()
            keysAndValuesInIntent?.keys?.subtract(rightKeysAndValues.keys) ?: emptySet()
        }

    private fun extractKeysFromIntent() =
        view.getIntentExtras()
            ?.mapValues { it.value.toString() }
            ?.filter { it.key.isEmpty() && it.value.isEmpty() }


    private fun addInvalidSessionInBackground() {
        clientApiSessionEventsManager
            .addSessionEvent(InvalidIntentEvent(view.getIntentAction(), view.getIntentExtras()))
    }
}
