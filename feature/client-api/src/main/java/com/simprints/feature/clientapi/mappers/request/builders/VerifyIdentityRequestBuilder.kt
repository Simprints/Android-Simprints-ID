package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.clientapi.mappers.request.extractors.VerifyIdentityRequestExtractor
import com.simprints.feature.clientapi.mappers.request.validators.VerifyIdentityValidator
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.orchestration.data.ActionRequest
import com.simprints.infra.orchestration.data.ActionRequestIdentifier

// TODO PoC
internal class VerifyIdentityRequestBuilder(
    private val actionIdentifier: ActionRequestIdentifier,
    private val extractor: VerifyIdentityRequestExtractor,
    private val project: Project?,
    private val tokenizationProcessor: TokenizationProcessor,
    validator: VerifyIdentityValidator,
) : ActionRequestBuilder(validator) {

    override fun encryptIfNecessary(actionRequest: ActionRequest): ActionRequest {
        val request = (actionRequest as? ActionRequest.VerifyActionRequest) ?: return actionRequest
        if (request.projectId != project?.id) return actionRequest

        val encryptedUserId = encryptField(
            value = request.userId,
            project = project,
            tokenKeyType = TokenKeyType.AttendantId,
            tokenizationProcessor = tokenizationProcessor
        )
        val encryptedModuleId = encryptField(
            value = request.moduleId,
            project = project,
            tokenKeyType = TokenKeyType.ModuleId,
            tokenizationProcessor = tokenizationProcessor
        )
        return request.copy(userId = encryptedUserId, moduleId = encryptedModuleId)
    }

    override fun buildAction(): ActionRequest = ActionRequest.VerifyIdentityRequest(
        actionIdentifier = actionIdentifier,
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId().asTokenizableRaw(),
        moduleId = extractor.getModuleId().asTokenizableRaw(),
        biometricDataSource = extractor.getBiometricDataSource(),
        metadata = extractor.getMetadata(),
        image = extractor.getImage(),
        subjectGuid = extractor.getSubjectGuid(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
