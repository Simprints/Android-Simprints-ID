package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_CONFIRMATION
import com.simprints.infra.events.event.domain.models.EventType.Companion.CALLBACK_CONFIRMATION_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(CALLBACK_CONFIRMATION_KEY)
data class ConfirmationCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ConfirmationCallbackPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        identificationOutcome: Boolean,
    ) : this(
        UUID.randomUUID().toString(),
        ConfirmationCallbackPayload(createdAt, EVENT_VERSION, identificationOutcome),
        CALLBACK_CONFIRMATION,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    @Serializable
    data class ConfirmationCallbackPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val identificationOutcome: Boolean,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLBACK_CONFIRMATION,
    ) : EventPayload() {
        override fun toSafeString(): String = "outcome: $identificationOutcome"
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
