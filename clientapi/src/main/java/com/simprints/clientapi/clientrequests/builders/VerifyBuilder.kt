package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.VerifyRequest
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.config.tokenization.TokenizationManager


class VerifyBuilder(
    private val extractor: VerifyExtractor,
    private val project: Project,
    private val tokenizationManager: TokenizationManager,
    validator: VerifyValidator
) : ClientRequestBuilder(validator) {
    override fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest {
        val request = (baseRequest as? VerifyRequest) ?: return baseRequest
        val encryptedUserId =
            encryptField(request.userId, project, TokenKeyType.AttendantId, tokenizationManager)
        val encryptedModuleId =
            encryptField(request.moduleId, project, TokenKeyType.ModuleId, tokenizationManager)
        return request.copy(userId = encryptedUserId, moduleId = encryptedModuleId)
    }

    override fun buildAppRequest(): BaseRequest = VerifyRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetadata(),
        verifyGuid = extractor.getVerifyGuid(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
