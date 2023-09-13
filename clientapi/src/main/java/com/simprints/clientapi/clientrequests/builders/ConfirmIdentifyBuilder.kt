package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.extractors.ConfirmIdentityExtractor
import com.simprints.clientapi.clientrequests.validators.ConfirmIdentityValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.requests.ConfirmIdentityRequest
import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.config.tokenization.TokenizationManager


class ConfirmIdentifyBuilder(
    private val extractor: ConfirmIdentityExtractor,
    private val project: Project?,
    private val tokenizationManager: TokenizationManager,
    validator: ConfirmIdentityValidator
) : ClientRequestBuilder(validator) {
    override fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest {
        val request = (baseRequest as? ConfirmIdentityRequest) ?: return baseRequest
        val encryptedUserId =
            encryptField(
                value = request.userId,
                project = project,
                tokenKeyType = TokenKeyType.AttendantId,
                tokenizationManager = tokenizationManager
            )
        return request.copy(userId = encryptedUserId)
    }

    override fun buildAppRequest(): BaseRequest = ConfirmIdentityRequest(
        projectId = extractor.getProjectId(),
        userId = extractor.getUserId().asTokenizedRaw(),
        sessionId = extractor.getSessionId(),
        selectedGuid = extractor.getSelectedGuid(),
        unknownExtras = extractor.getUnknownExtras()
    )
}
