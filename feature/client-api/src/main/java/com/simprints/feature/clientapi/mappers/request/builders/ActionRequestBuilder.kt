package com.simprints.feature.clientapi.mappers.request.builders

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.clientapi.mappers.request.validators.RequestActionValidator
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.orchestration.data.ActionRequest

internal abstract class ActionRequestBuilder(
    private val validator: RequestActionValidator,
) {
    protected abstract fun buildAction(): ActionRequest

    protected abstract fun encryptIfNecessary(actionRequest: ActionRequest): ActionRequest

    protected fun encryptField(
        value: TokenizableString,
        project: Project?,
        tokenKeyType: TokenKeyType,
        tokenizationProcessor: TokenizationProcessor,
    ): TokenizableString = if (project != null && value is TokenizableString.Raw) {
        tokenizationProcessor.encrypt(
            decrypted = value,
            tokenKeyType = tokenKeyType,
            project = project,
        )
    } else {
        value
    }

    fun build(): ActionRequest {
        validator.validate()
        return buildAction().run(::encryptIfNecessary)
    }
}
