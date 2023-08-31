package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.infra.config.domain.TokenizationAction
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.config.tokenization.TokenizationManager


abstract class ClientRequestBuilder(private val validator: ClientRequestValidator) {

    protected abstract fun buildAppRequest(): BaseRequest
    protected abstract fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest

    protected fun encryptField(
        value: String,
        project: Project,
        tokenKeyType: TokenKeyType,
        tokenizationManager: TokenizationManager
    ): String = tokenizationManager.tryTokenize(
        value = value,
        tokenKeyType = tokenKeyType,
        action = TokenizationAction.Encrypt,
        project = project
    )

    fun build(): BaseRequest {
        validator.validateClientRequest()
        return buildAppRequest().run(::encryptIfNecessary)
    }

}
