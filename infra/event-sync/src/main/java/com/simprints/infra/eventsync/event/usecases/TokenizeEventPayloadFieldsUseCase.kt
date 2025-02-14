package com.simprints.infra.eventsync.event.usecases

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import javax.inject.Inject
import com.simprints.infra.events.event.domain.models.Event

internal class TokenizeEventPayloadFieldsUseCase @Inject constructor(
    private val tokenizationProcessor: TokenizationProcessor
) {
    operator fun invoke(event: Event, project: Project): Event {
        val tokenizedFields = event.getTokenizableFields().mapValues { entry ->
            val tokenKeyType = entry.key
            return@mapValues when (val tokenizableField = entry.value) {
                is TokenizableString.Raw -> {
                    tokenizationProcessor.encrypt(
                        decrypted = tokenizableField,
                        tokenKeyType = tokenKeyType,
                        project = project
                    )
                }

                is TokenizableString.Tokenized -> tokenizableField
            }
        }
        return event.setTokenizedFields(tokenizedFields)
    }
}
