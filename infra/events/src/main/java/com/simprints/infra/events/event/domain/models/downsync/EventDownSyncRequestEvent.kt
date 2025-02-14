package com.simprints.infra.events.event.domain.models.downsync

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import java.util.UUID

@Keep
data class EventDownSyncRequestEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: EventDownSyncRequestPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        endedAt: Timestamp,
        query: QueryParameters,
        requestId: String,
        responseStatus: Int? = null,
        errorType: String? = null,
        msToFirstResponseByte: Long? = null,
        eventRead: Int? = null,
    ) : this(
        UUID.randomUUID().toString(),
        EventDownSyncRequestPayload(
            createdAt,
            endedAt,
            requestId,
            query,
            responseStatus,
            errorType,
            msToFirstResponseByte,
            eventRead,
            EVENT_VERSION,
        ),
        EventType.EVENT_DOWN_SYNC_REQUEST,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = listOf(
        payload.queryParameters.attendantId?.let { TokenKeyType.AttendantId to TokenizableString.Tokenized(it) },
        payload.queryParameters.moduleId?.let { TokenKeyType.ModuleId to TokenizableString.Tokenized(it) },
    ).mapNotNull { it }.toMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>): Event = this

    @Keep
    data class EventDownSyncRequestPayload(
        override val createdAt: Timestamp,
        override val endedAt: Timestamp?,
        val requestId: String,
        val queryParameters: QueryParameters,
        val responseStatus: Int?,
        val errorType: String?,
        val msToFirstResponseByte: Long?,
        val eventsRead: Int?,
        override val eventVersion: Int,
        override val type: EventType = EventType.EVENT_DOWN_SYNC_REQUEST,
    ) : EventPayload() {
        override fun toSafeString(): String = "request ID: $requestId, status: $responseStatus, error: $errorType, " +
            "ms to response: $msToFirstResponseByte, events read: $eventsRead"
    }

    @Keep
    data class QueryParameters(
        val moduleId: String? = null,
        val attendantId: String? = null,
        val subjectId: String? = null,
        val modes: List<String>? = null,
        val lastEventId: String? = null,
    )

    companion object {
        const val EVENT_VERSION = 0
    }
}
