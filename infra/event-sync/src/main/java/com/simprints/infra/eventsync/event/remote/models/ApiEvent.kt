package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event

@Keep
internal data class ApiEvent(
    val id: String,
    val type: ApiEventPayloadType,
    val payload: ApiEventPayload,
    val tokenizedFields: List<String>,
)

internal fun Event.fromDomainToApi(): ApiEvent {
    val tokenizedKeyTypes =
        getTokenizedFields().filter { it.value is TokenizableString.Tokenized }.keys.toList()
    val payload = payload.fromDomainToApi()
    val tokenizedFields = tokenizedKeyTypes.mapNotNull(payload::getTokenizedFieldJsonPath)

    return ApiEvent(
        id = id,
        type = type.fromDomainToApi(),
        payload = payload,
        tokenizedFields = tokenizedFields
    )
}
