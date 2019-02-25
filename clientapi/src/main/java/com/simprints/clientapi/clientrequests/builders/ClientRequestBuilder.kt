package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.ClientRequestExtractor
import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator
import com.simprints.clientapi.simprintsrequests.requests.ClientApiBaseRequest
import com.simprints.clientapi.simprintsrequests.requests.legacy.LegacySimprintsIdRequest


abstract class ClientRequestBuilder(private val extractor: ClientRequestExtractor,
                                    private val validator: ClientRequestValidator) {

    protected abstract fun buildSimprintsRequest(): ClientApiBaseRequest

    protected abstract fun buildLegacySimprintsRequest(): LegacySimprintsIdRequest

    open fun isLegacyRequest(): Boolean = !extractor.getLegacyApiKey().isBlank()

    fun build(): ClientApiBaseRequest {
        validator.validateClientRequest()
        return if (isLegacyRequest())
            buildLegacySimprintsRequest()
        else
            buildSimprintsRequest()
    }

}
