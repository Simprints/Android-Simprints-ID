package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.extractors.EnrolRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.EnrolValidator


internal class EnrolRequestBuilder(
    val packageName: String,
    private val extractor: EnrolRequestExtractor,
    validator: EnrolValidator,
) : ActionRequestBuilder(validator) {

    override fun buildAction(): ActionRequest = ActionRequest.EnrolActionRequest(
        packageName = packageName,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        metadata = extractor.getMetadata(),
        moduleId = extractor.getModuleId(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
