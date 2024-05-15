package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.events.event.domain.models.Event

@Keep
internal data class ApiEvent(
    val id: String,
    val type: ApiEventPayloadType,
    val version: Int,
    val payload: ApiEventPayload,
    val tokenizedFields: List<String>,
)

internal fun Event.fromDomainToApi(): ApiEvent {
    val tokenizedKeyTypes =
        getTokenizedFields().filter { it.value is TokenizableString.Tokenized }.keys.toList()
    val apiPayload = payload.fromDomainToApi()
    val tokenizedFields = tokenizedKeyTypes.mapNotNull(apiPayload::getTokenizedFieldJsonPath)

    return ApiEvent(
        id = id,
        type = type.fromDomainToApi(),
        version = payload.eventVersion,
        payload = apiPayload,
        tokenizedFields = tokenizedFields
    )
}
