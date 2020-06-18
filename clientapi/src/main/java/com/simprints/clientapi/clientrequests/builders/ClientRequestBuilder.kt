package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator
import com.simprints.clientapi.domain.requests.BaseRequest


abstract class ClientRequestBuilder(private val validator: ClientRequestValidator) {

    protected abstract fun buildAppRequest(): BaseRequest

    fun build(): BaseRequest {
        validator.validateClientRequest()
        return buildAppRequest()
    }

}
