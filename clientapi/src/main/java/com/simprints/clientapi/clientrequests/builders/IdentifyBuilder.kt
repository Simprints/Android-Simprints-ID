package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.IdentifyExtractor
import com.simprints.clientapi.clientrequests.validators.IdentifyValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.IdentifyRequest
import com.simprints.core.tools.utils.Tokenization
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType


class IdentifyBuilder(
    private val extractor: IdentifyExtractor,
    private val project: Project,
    private val tokenization: Tokenization,
    validator: IdentifyValidator
) : ClientRequestBuilder(validator) {
    override fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest {
        val request = (baseRequest as? IdentifyRequest) ?: return baseRequest
        val encryptedUserId =
            encryptField(request.userId, project, TokenKeyType.AttendantId, tokenization)
        val encryptedModuleId =
            encryptField(request.userId, project, TokenKeyType.ModuleId, tokenization)
        return request.copy(userId = encryptedUserId, moduleId = encryptedModuleId)
    }

    override fun buildAppRequest(): BaseRequest = IdentifyRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId(),
        moduleId = extractor.getModuleId(),
        metadata = extractor.getMetadata(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
