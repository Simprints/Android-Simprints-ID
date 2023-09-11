package com.simprints.infra.events.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizedString
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_CONFIRMATION
import java.util.UUID

@Keep
data class ConfirmationCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels = EventLabels(),
    override val payload: ConfirmationCallbackPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        identificationOutcome: Boolean,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ConfirmationCallbackPayload(createdAt, EVENT_VERSION, identificationOutcome),
        CALLBACK_CONFIRMATION
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizedString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizedString>) = this // No tokenized fields

    @Keep
    data class ConfirmationCallbackPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val identificationOutcome: Boolean,
        override val type: EventType = CALLBACK_CONFIRMATION,
        override val endedAt: Long = 0,
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
