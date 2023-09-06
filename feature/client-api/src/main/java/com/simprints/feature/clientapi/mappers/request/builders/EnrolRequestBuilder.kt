package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.extractors.EnrolRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.EnrolValidator
import com.simprints.feature.clientapi.models.ActionRequestIdentifier


internal class EnrolRequestBuilder(
    private val actionIdentifier: ActionRequestIdentifier,
    private val extractor: EnrolRequestExtractor,
    validator: EnrolValidator,
) : ActionRequestBuilder(validator) {

    override fun buildAction(): ActionRequest = ActionRequest.EnrolActionRequest(
        actionIdentifier = actionIdentifier,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        metadata = extractor.getMetadata(),
        moduleId = extractor.getModuleId(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
