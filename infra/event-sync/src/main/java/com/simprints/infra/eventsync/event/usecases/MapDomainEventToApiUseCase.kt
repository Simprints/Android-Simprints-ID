package com.simprints.infra.eventsync.event.usecases

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.eventsync.event.remote.models.ApiEvent
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi
import javax.inject.Inject

internal class MapDomainEventToApiUseCase @Inject constructor(
    private val tokenizeEventPayloadFieldsUseCase: TokenizeEventPayloadFieldsUseCase,
) {
    operator fun invoke(event: Event, project: Project): ApiEvent = with(tokenizeEventPayloadFieldsUseCase(event, project)) {
        val apiPayload = payload.fromDomainToApi()
        val tokenizedKeyTypes = getTokenizableFields().filter { it.value is TokenizableString.Tokenized }.keys.toList()
        val tokenizedFields = tokenizedKeyTypes.mapNotNull(apiPayload::getTokenizedFieldJsonPath)
        ApiEvent(
            id = id,
            type = type.fromDomainToApi(),
            version = payload.eventVersion,
            payload = apiPayload,
            tokenizedFields = tokenizedFields,
        )
    }

}
