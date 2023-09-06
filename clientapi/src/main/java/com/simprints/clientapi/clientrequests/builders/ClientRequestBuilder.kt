package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.core.domain.tokenization.TokenizedString
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.config.tokenization.TokenizationManager


abstract class ClientRequestBuilder(private val validator: ClientRequestValidator) {

    protected abstract fun buildAppRequest(): BaseRequest
    protected abstract fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest

    protected fun encryptField(
        value: TokenizedString,
        project: Project?,
        tokenKeyType: TokenKeyType,
        tokenizationManager: TokenizationManager
    ): TokenizedString = if (project == null) value else tokenizationManager.encrypt(
        decrypted = value,
        tokenKeyType = tokenKeyType,
        project = project
    )

    fun build(): BaseRequest {
        validator.validateClientRequest()
        return buildAppRequest().run(::encryptIfNecessary)
    }

}
