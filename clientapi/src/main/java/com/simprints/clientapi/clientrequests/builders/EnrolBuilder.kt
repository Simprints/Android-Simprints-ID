package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.EnrolExtractor
import com.simprints.clientapi.clientrequests.validators.EnrolValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.EnrolRequest
import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.sync.tokenization.TokenizationManager


class EnrolBuilder(
    private val extractor: EnrolExtractor,
    private val project: Project?,
    private val tokenizationManager: TokenizationManager,
    validator: EnrolValidator
) : ClientRequestBuilder(validator) {
    override fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest {
        val request = (baseRequest as? EnrolRequest) ?: return baseRequest
        if (request.projectId != project?.id) return baseRequest

        val encryptedUserId = encryptField(
            value = request.userId,
            project = project,
            tokenKeyType = TokenKeyType.AttendantId,
            tokenizationManager = tokenizationManager
        )
        val encryptedModuleId = encryptField(
            value = request.moduleId,
            project = project,
            tokenKeyType = TokenKeyType.ModuleId,
            tokenizationManager = tokenizationManager
        )
        return request.copy(userId = encryptedUserId, moduleId = encryptedModuleId)
    }

    override fun buildAppRequest(): BaseRequest = EnrolRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId().asTokenizedRaw(),
        metadata = extractor.getMetadata(),
        moduleId = extractor.getModuleId().asTokenizedRaw(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
