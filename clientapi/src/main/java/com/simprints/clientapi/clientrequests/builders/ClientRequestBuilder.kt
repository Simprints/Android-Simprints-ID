package com.simprints.clientapi.clientrequests.builders

import com.simprints.clientapi.clientrequests.validators.ClientRequestValidator
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.core.tools.utils.Tokenization
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.domain.models.TokenKeyType


abstract class ClientRequestBuilder(private val validator: ClientRequestValidator) {

    protected abstract fun buildAppRequest(): BaseRequest
    protected abstract fun encryptIfNecessary(baseRequest: BaseRequest): BaseRequest

    protected fun encryptField(
        value: String,
        project: Project,
        tokenKeyType: TokenKeyType,
        tokenization: Tokenization
    ): String {
        val keysetJson = project.tokenizationKeys[tokenKeyType] ?: return value
        return runCatching { tokenization.encrypt(value, keysetJson) }.getOrElse { value }
    }

    fun build(): BaseRequest {
        validator.validateClientRequest()
        return buildAppRequest().run(::encryptIfNecessary)
    }

}
