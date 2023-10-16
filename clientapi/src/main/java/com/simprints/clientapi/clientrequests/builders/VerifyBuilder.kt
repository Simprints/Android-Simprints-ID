package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.VerifyRequest
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor


class VerifyBuilder(
    private val extractor: VerifyExtractor,
    private val project: Project?,
    private val tokenizationProcessor: TokenizationProcessor,
    validator: VerifyValidator
) : ClientRequestBuilder(validator) {
    override fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest {
        val request = (baseRequest as? VerifyRequest) ?: return baseRequest
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

    override fun buildAppRequest(): BaseRequest = VerifyRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId().asTokenizableRaw(),
        moduleId = extractor.getModuleId().asTokenizableRaw(),
        metadata = extractor.getMetadata(),
        verifyGuid = extractor.getVerifyGuid(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
