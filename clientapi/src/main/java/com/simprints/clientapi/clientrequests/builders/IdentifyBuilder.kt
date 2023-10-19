package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.IdentifyRequest
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor


class IdentifyBuilder(
    private val extractor: IdentifyExtractor,
    private val project: Project?,
    private val tokenizationProcessor: TokenizationProcessor,
    validator: IdentifyValidator
) : ClientRequestBuilder(validator) {
    override fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest {
        val request = (baseRequest as? IdentifyRequest) ?: return baseRequest
        if (request.projectId != project?.id) return baseRequest

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

    override fun buildAppRequest(): BaseRequest = IdentifyRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId().asTokenizableRaw(),
        moduleId = extractor.getModuleId().asTokenizableRaw(),
        metadata = extractor.getMetadata(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
