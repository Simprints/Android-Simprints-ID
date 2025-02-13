package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.eventsync.event.usecases.TokenizeEventPayloadFieldsUseCase

@Keep
internal data class ApiEvent(
    val id: String,
    val type: ApiEventPayloadType,
    val version: Int,
    val payload: ApiEventPayload,
    val tokenizedFields: List<String>,
)

internal fun Event.fromDomainToApi(tokenizeEventPayloadFieldsUseCase: TokenizeEventPayloadFieldsUseCase, project: Project): ApiEvent {
    with(tokenizeEventPayloadFieldsUseCase(this, project)) {
        val apiPayload = payload.fromDomainToApi()
        val tokenizedKeyTypes = getTokenizedFields().filter { it.value is TokenizableString.Tokenized }.keys.toList()
        val tokenizedFields = tokenizedKeyTypes.mapNotNull(apiPayload::getTokenizedFieldJsonPath)

        return ApiEvent(
            id = id,
            type = type.fromDomainToApi(),
            version = payload.eventVersion,
            payload = apiPayload,
            tokenizedFields = tokenizedFields,
        )

    }
}
