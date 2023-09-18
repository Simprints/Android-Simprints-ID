package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.feature.clientapi.mappers.request.extractors.EnrolLastBiometricsRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.EnrolLastBiometricsValidator
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier

internal class EnrolLastBiometricsRequestBuilder(
    private val actionIdentifier: ActionRequestIdentifier,
    private val extractor: EnrolLastBiometricsRequestExtractor,
    validator: EnrolLastBiometricsValidator,
) : ActionRequestBuilder(validator) {

    override fun buildAction(): ActionRequest = ActionRequest.EnrolLastBiometricActionRequest(
        actionIdentifier = actionIdentifier,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetadata(),
        sessionId = extractor.getSessionId(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
