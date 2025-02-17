package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.GUID_SELECTION
import java.util.UUID

@Keep
data class GuidSelectionEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: GuidSelectionPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        selectedId: String,
    ) : this(
        UUID.randomUUID().toString(),
        GuidSelectionPayload(createdAt, EVENT_VERSION, selectedId),
        GUID_SELECTION,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class GuidSelectionPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val selectedId: String,
        override val endedAt: Timestamp? = null,
        override val type: EventType = GUID_SELECTION,
    ) : EventPayload() {
        override fun toSafeString(): String = "guid: $selectedId"
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
