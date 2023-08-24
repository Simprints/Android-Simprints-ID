package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.extractors.EnrolLastBiometricsRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.EnrolLastBiometricsValidator

internal class EnrolLastBiometricsRequestBuilder(
    val packageName: String,
    private val extractor: EnrolLastBiometricsRequestExtractor,
    validator: EnrolLastBiometricsValidator,
) : ActionRequestBuilder(validator) {

    override fun buildAction(): ActionRequest = ActionRequest.EnrolLastBiometricActionRequest(
        packageName = packageName,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetadata(),
        sessionId = extractor.getSessionId(),
        unknownExtras = extractor.getUnknownExtras()
    )
}

