package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.EnrolLastBiometricsExtractor
import com.simprints.clientapi.clientrequests.validators.EnrolLastBiometricsValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.EnrolLastBiometricsRequest
import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.config.tokenization.TokenizationManager


class EnrolLastBiometricsBuilder(
    private val extractor: EnrolLastBiometricsExtractor,
    private val project: Project?,
    private val tokenizationManager: TokenizationManager,
    validator: EnrolLastBiometricsValidator
) : ClientRequestBuilder(validator) {
    override fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest {
        val request = (baseRequest as? EnrolLastBiometricsRequest) ?: return baseRequest
        val encryptedUserId =
            encryptField(request.userId, project, TokenKeyType.AttendantId, tokenizationManager)
        val encryptedModuleId =
            encryptField(request.moduleId, project, TokenKeyType.ModuleId, tokenizationManager)
        return request.copy(userId = encryptedUserId, moduleId = encryptedModuleId)
    }

    override fun buildAppRequest(): BaseRequest = EnrolLastBiometricsRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId().asTokenizedRaw(),
        moduleId = extractor.getModuleId().asTokenizedRaw(),
        metadata = extractor.getMetadata(),
        sessionId = extractor.getSessionId(),
        unknownExtras = extractor.getUnknownExtras()
    )
}

