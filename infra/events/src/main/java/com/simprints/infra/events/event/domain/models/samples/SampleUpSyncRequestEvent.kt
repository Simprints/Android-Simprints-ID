package com.simprints.infra.events.event.domain.models.samples

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import java.util.UUID

@Keep
class SampleUpSyncRequestEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: SampleUpSyncRequestPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        endedAt: Timestamp,
        requestId: String,
        sampleId: String,
        size: Long,
        errorType: String? = null,
    ) : this(
        UUID.randomUUID().toString(),
        SampleUpSyncRequestPayload(
            createdAt,
            endedAt,
            requestId,
            sampleId,
            size,
            errorType,
            EVENT_VERSION,
        ),
        EventType.SAMPLE_UP_SYNC_REQUEST,
    )

    @Keep
    data class SampleUpSyncRequestPayload(
        override val createdAt: Timestamp,
        override val endedAt: Timestamp?,
        val requestId: String,
        val sampleId: String,
        val size: Long,
        val errorType: String?,
        override val eventVersion: Int,
        override val type: EventType = EventType.SAMPLE_UP_SYNC_REQUEST,
    ) : EventPayload() {
        override fun toSafeString(): String = "request ID: $requestId, error: $errorType," +
            "sample: $sampleId, sample size: $size"
    }

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>): Event = this

    companion object Companion {
        const val EVENT_VERSION = 0
    }
}
