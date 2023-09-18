package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.feature.clientapi.mappers.request.validators.RequestActionValidator
import com.simprints.infra.orchestration.data.ActionRequest


internal abstract class ActionRequestBuilder(private val validator: RequestActionValidator) {

    protected abstract fun buildAction(): ActionRequest

    fun build(): ActionRequest {
        validator.validate()
        return buildAction()
    }

}