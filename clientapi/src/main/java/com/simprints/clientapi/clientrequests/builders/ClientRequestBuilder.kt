package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator
import com.simprints.clientapi.simprintsrequests.requests.ClientApiBaseRequest


abstract class ClientRequestBuilder(private val validator: ClientRequestValidator) {

    protected abstract fun buildSimprintsRequest(): ClientApiBaseRequest

    fun build(): ClientApiBaseRequest {
        validator.validateClientRequest()
        return buildSimprintsRequest()
    }

}
