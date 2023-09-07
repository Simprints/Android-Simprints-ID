package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.feature.clientapi.mappers.request.extractors.VerifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.VerifyValidator
import com.simprints.feature.orchestrator.models.ActionRequest
import com.simprints.feature.orchestrator.models.ActionRequestIdentifier


internal class VerifyRequestBuilder(
    private val actionIdentifier: ActionRequestIdentifier,
    private val extractor: VerifyRequestExtractor,
    validator: VerifyValidator,
) : ActionRequestBuilder(validator) {

    override fun buildAction(): ActionRequest = ActionRequest.VerifyActionRequest(
        actionIdentifier = actionIdentifier,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetadata(),
        verifyGuid = extractor.getVerifyGuid(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
