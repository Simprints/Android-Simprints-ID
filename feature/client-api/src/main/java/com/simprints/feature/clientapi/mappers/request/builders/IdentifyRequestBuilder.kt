package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.feature.clientapi.mappers.request.extractors.IdentifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.IdentifyValidator
import com.simprints.feature.orchestrator.models.ActionRequest
import com.simprints.feature.orchestrator.models.ActionRequestIdentifier


internal class IdentifyRequestBuilder(
    private val actionIdentifier: ActionRequestIdentifier,
    val extractor: IdentifyRequestExtractor,
    validator: IdentifyValidator,
) : ActionRequestBuilder(validator) {

    override fun buildAction(): ActionRequest = ActionRequest.IdentifyActionRequest(
        actionIdentifier = actionIdentifier,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetadata(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
