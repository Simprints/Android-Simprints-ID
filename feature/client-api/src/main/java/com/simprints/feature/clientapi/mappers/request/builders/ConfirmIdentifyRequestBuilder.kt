package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.feature.clientapi.mappers.request.extractors.ConfirmIdentityRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.ConfirmIdentityValidator
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier


internal class ConfirmIdentifyRequestBuilder(
    private val actionIdentifier: ActionRequestIdentifier,
    val extractor: ConfirmIdentityRequestExtractor,
    validator: ConfirmIdentityValidator,
) : ActionRequestBuilder(validator) {

    override fun buildAction(): ActionRequest = ActionRequest.ConfirmIdentityActionRequest(
        actionIdentifier = actionIdentifier,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        sessionId = extractor.getSessionId(),
        selectedGuid = extractor.getSelectedGuid(),
        unknownExtras = extractor.getUnknownExtras(),
    )
}
