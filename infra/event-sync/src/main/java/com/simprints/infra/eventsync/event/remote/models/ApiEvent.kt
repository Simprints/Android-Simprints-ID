package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.eventsync.event.remote.models.ApiEvent.Companion.CALLOUT_MODULE_ID
import com.simprints.infra.eventsync.event.remote.models.ApiEvent.Companion.CALLOUT_USER_ID

@Keep
internal data class ApiEvent(
    val id: String,
    val labels: ApiEventLabels,
    val payload: ApiEventPayload,
    val tokenizedFields: List<String>
) {
    companion object {
        private const val CALLOUT_PREFIX = "callout"
        const val CALLOUT_MODULE_ID = "$CALLOUT_PREFIX.moduleId"
        const val CALLOUT_USER_ID = "$CALLOUT_PREFIX.userId"
    }
}

internal fun Event.fromDomainToApi(): ApiEvent {
    val tokenizedFields = getTokenizedFields().mapNotNull { tokenizedFieldEntry ->
        when (tokenizedFieldEntry.value) {
            is TokenizableString.Raw -> null
            is TokenizableString.Tokenized -> tokenizedFieldEntry.key.mapToTokenizedString()
        }
    }
    return ApiEvent(
        id = id,
        labels = labels.fromDomainToApi(),
        payload = payload.fromDomainToApi(),
        tokenizedFields = tokenizedFields
    )
}

internal fun TokenKeyType.mapToTokenizedString(): String? = when (this) {
    TokenKeyType.AttendantId -> CALLOUT_USER_ID
    TokenKeyType.ModuleId -> CALLOUT_MODULE_ID
    TokenKeyType.Unknown -> null
}