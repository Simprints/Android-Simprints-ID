package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.feature.clientapi.models.ActionRequest
import com.simprints.feature.clientapi.mappers.request.extractors.ConfirmIdentityRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.ConfirmIdentityValidator


internal class ConfirmIdentifyRequestBuilder(
    val packageName: String,
    val extractor: ConfirmIdentityRequestExtractor,
    validator: ConfirmIdentityValidator,
) : ActionRequestBuilder(validator) {

    override fun buildAction(): ActionRequest = ActionRequest.ConfirmActionRequest(
        packageName = packageName,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        sessionId = extractor.getSessionId(),
        selectedGuid = extractor.getSelectedGuid(),
        unknownExtras = extractor.getUnknownExtras(),
    )
}
