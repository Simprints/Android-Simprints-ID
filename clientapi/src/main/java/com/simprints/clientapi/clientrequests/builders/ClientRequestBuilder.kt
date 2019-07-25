package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator
import com.simprints.clientapi.domain.ClientBase


abstract class ClientRequestBuilder(private val validator: ClientRequestValidator) {

    protected abstract fun buildAppRequest(): ClientBase

    fun build(): ClientBase {
        validator.validateClientRequest()
        return buildAppRequest()
    }

}
