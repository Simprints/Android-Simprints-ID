package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.clientapi.mappers.request.extractors.ConfirmIdentityRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.ConfirmIdentityValidator
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier


internal class ConfirmIdentifyRequestBuilder(
    private val actionIdentifier: ActionRequestIdentifier,
    val extractor: ConfirmIdentityRequestExtractor,
    private val project: Project?,
    private val tokenizationProcessor: TokenizationProcessor,
    validator: ConfirmIdentityValidator,
) : ActionRequestBuilder(validator) {
    override fun encryptIfNecessary(actionRequest: ActionRequest): ActionRequest {
        val request = (actionRequest as? ActionRequest.ConfirmIdentityActionRequest) ?: return actionRequest
        if (request.projectId != project?.id) return actionRequest

        val encryptedUserId = encryptField(
            value = request.userId,
            project = project,
            tokenKeyType = TokenKeyType.AttendantId,
            tokenizationProcessor = tokenizationProcessor
        )
        return request.copy(userId = encryptedUserId)
    }

    override fun buildAction(): ActionRequest = ActionRequest.ConfirmIdentityActionRequest(
        actionIdentifier = actionIdentifier,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId().asTokenizableRaw(),
        sessionId = extractor.getSessionId(),
        selectedGuid = extractor.getSelectedGuid(),
        metadata = extractor.getMetadata(),
        unknownExtras = extractor.getUnknownExtras(),
    )
}
