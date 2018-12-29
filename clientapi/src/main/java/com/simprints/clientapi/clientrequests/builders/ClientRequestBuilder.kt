package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.requests.ClientRequest
import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator


abstract class ClientRequestBuilder(private val extractor: ClientRequestExtractor,
                                    private val validator: ClientRequestValidator) {

    protected abstract fun buildClientRequest(): ClientRequest

    protected abstract fun buildLegacyClientRequest(): ClientRequest

    protected open fun isLegacyRequest(): Boolean = !extractor.getLegacyApiKey().isNullOrBlank()

    fun build(): ClientRequest {
        validator.validateClientRequest()
        return if (isLegacyRequest())
            buildLegacyClientRequest()
        else
            buildClientRequest()
    }

}
