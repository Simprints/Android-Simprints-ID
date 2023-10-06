package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationManager


abstract class ClientRequestBuilder(private val validator: ClientRequestValidator) {

    protected abstract fun buildAppRequest(): BaseRequest
    protected abstract fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest

    protected fun encryptField(
        value: TokenizableString,
        project: Project?,
        tokenKeyType: TokenKeyType,
        tokenizationManager: TokenizationManager
    ): TokenizableString =
        if (project != null && value is TokenizableString.Raw) tokenizationManager.encrypt(
            decrypted = value,
            tokenKeyType = tokenKeyType,
            project = project
        ) else value

    fun build(): BaseRequest {
        validator.validateClientRequest()
        return buildAppRequest().run(::encryptIfNecessary)
    }

}
