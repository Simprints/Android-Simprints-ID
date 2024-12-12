package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.clientapi.mappers.request.extractors.VerifyRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.VerifyValidator
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier

internal class VerifyRequestBuilder(
    private val actionIdentifier: ActionRequestIdentifier,
    private val extractor: VerifyRequestExtractor,
    private val project: Project?,
    private val tokenizationProcessor: TokenizationProcessor,
    validator: VerifyValidator,
) : ActionRequestBuilder(validator) {
    override fun encryptIfNecessary(actionRequest: ActionRequest): ActionRequest {
        val request = (actionRequest as? ActionRequest.VerifyActionRequest) ?: return actionRequest
        if (request.projectId != project?.id) return actionRequest

        val encryptedUserId = encryptField(
            value = request.userId,
            project = project,
            tokenKeyType = TokenKeyType.AttendantId,
            tokenizationProcessor = tokenizationProcessor,
        )
        val encryptedModuleId = encryptField(
            value = request.moduleId,
            project = project,
            tokenKeyType = TokenKeyType.ModuleId,
            tokenizationProcessor = tokenizationProcessor,
        )
        return request.copy(userId = encryptedUserId, moduleId = encryptedModuleId)
    }

    override fun buildAction(): ActionRequest = ActionRequest.VerifyActionRequest(
        actionIdentifier = actionIdentifier,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId().asTokenizableRaw(),
        moduleId = extractor.getModuleId().asTokenizableRaw(),
        biometricDataSource = extractor.getBiometricDataSource(),
        subjectAge = extractor.getSubjectAge(),
        metadata = extractor.getMetadata(),
        verifyGuid = extractor.getVerifyGuid(),
        unknownExtras = extractor.getUnknownExtras(),
    )
}
