package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.extractors.IdentifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.IdentifyValidator


internal class IdentifyRequestBuilder(
    val packageName: String, val extractor: IdentifyRequestExtractor,
    validator: IdentifyValidator,
) : ActionRequestBuilder(validator) {

    override fun buildAction(): ActionRequest = ActionRequest.IdentifyActionRequest(
        packageName = packageName,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetadata(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
