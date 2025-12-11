package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_CONFIRMATION
import com.simprints.infra.events.event.domain.models.EventType.Companion.CALLOUT_CONFIRMATION_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(CALLOUT_CONFIRMATION_KEY)
@Deprecated("Replaced by v3 in 2025.2.0")
data class ConfirmationCalloutEventV2(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ConfirmationCalloutPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        projectId: String,
        selectedGuid: String,
        sessionId: String,
        metadata: String?,
    ) : this(
        UUID.randomUUID().toString(),
        ConfirmationCalloutPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            projectId = projectId,
            selectedGuid = selectedGuid,
            sessionId = sessionId,
            metadata = metadata,
        ),
        CALLOUT_CONFIRMATION,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    @Serializable
    data class ConfirmationCalloutPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val projectId: String,
        val selectedGuid: String,
        val sessionId: String,
        val metadata: String? = null,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLOUT_CONFIRMATION,
    ) : EventPayload() {
        override fun toSafeString(): String = "guid: $selectedGuid, session ID: $sessionId"
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
