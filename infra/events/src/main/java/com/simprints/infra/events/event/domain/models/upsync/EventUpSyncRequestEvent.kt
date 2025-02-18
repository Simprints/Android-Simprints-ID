package com.simprints.infra.events.event.domain.models.upsync

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import java.util.UUID

@Keep
data class EventUpSyncRequestEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: EventUpSyncRequestPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        endedAt: Timestamp,
        requestId: String,
        content: UpSyncContent = UpSyncContent(),
        responseStatus: Int? = null,
        errorType: String? = null,
    ) : this(
        UUID.randomUUID().toString(),
        EventUpSyncRequestPayload(
            createdAt,
            endedAt,
            requestId,
            content,
            responseStatus,
            errorType,
            EVENT_VERSION,
        ),
        EventType.EVENT_UP_SYNC_REQUEST,
    )

    @Keep
    data class EventUpSyncRequestPayload(
        override val createdAt: Timestamp,
        override val endedAt: Timestamp?,
        val requestId: String,
        val content: UpSyncContent,
        val responseStatus: Int?,
        val errorType: String?,
        override val eventVersion: Int,
        override val type: EventType = EventType.EVENT_UP_SYNC_REQUEST,
    ) : EventPayload() {
        override fun toSafeString(): String = "request ID: $requestId, response: $responseStatus, error: $errorType," +
            "sessions: ${content.sessionCount}, eventsUp: ${content.eventUpSyncCount}, eventsDown: ${content.eventDownSyncCount}"
    }

    @Keep
    data class UpSyncContent(
        val sessionCount: Int = 0,
        val eventUpSyncCount: Int = 0,
        val eventDownSyncCount: Int = 0,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>): Event = this

    companion object {
        const val EVENT_VERSION = 0
    }
}
