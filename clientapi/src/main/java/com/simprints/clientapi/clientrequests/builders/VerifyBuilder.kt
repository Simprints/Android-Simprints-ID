package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.clientrequests.validators.VerifyValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.VerifyRequest
import com.simprints.core.tools.utils.Tokenization
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType


class VerifyBuilder(
    private val extractor: VerifyExtractor,
    private val project: Project,
    private val tokenization: Tokenization,
    validator: VerifyValidator
) : ClientRequestBuilder(validator) {
    override fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest {
        val request = (baseRequest as? VerifyRequest) ?: return baseRequest
        val encryptedUserId =
            encryptField(request.userId, project, TokenKeyType.AttendantId, tokenization)
        val encryptedModuleId =
            encryptField(request.userId, project, TokenKeyType.ModuleId, tokenization)
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
