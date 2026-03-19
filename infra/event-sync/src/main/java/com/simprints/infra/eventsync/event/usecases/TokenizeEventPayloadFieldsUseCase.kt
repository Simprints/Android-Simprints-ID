package com.simprints.infra.eventsync.event.usecases

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.events.event.domain.models.Event
import javax.inject.Inject

internal class TokenizeEventPayloadFieldsUseCase @Inject constructor(
    private val tokenizationProcessor: TokenizationProcessor,
) {
    operator fun invoke(
        event: Event,
        project: Project,
    ): Event {
        val tokenizedFields = event.getTokenizableFields().mapValues { (tokenKeyType, fieldValue) ->
            tokenizationProcessor.tokenizeIfNecessary(
                tokenizableString = fieldValue,
                tokenKeyType = tokenKeyType,
                project = project,
            )
        }
        val tokenizedListFields = event.getTokenizableListFields().mapValues { (tokenKeyType, listFieldValue) ->
            listFieldValue.map { fieldValue ->
                tokenizationProcessor.tokenizeIfNecessary(
                    tokenizableString = fieldValue,
                    tokenKeyType = tokenKeyType,
                    project = project,
                )
            }
        }
        return event.setTokenizedFields(tokenizedFields).setTokenizedListFields(tokenizedListFields)
    }
}
