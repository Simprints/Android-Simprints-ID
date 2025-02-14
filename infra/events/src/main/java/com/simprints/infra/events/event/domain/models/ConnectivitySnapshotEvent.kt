package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CONNECTIVITY_SNAPSHOT
import java.util.UUID

@Keep
data class ConnectivitySnapshotEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ConnectivitySnapshotPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        connections: List<SimNetworkUtils.Connection>,
    ) : this(
        UUID.randomUUID().toString(),
        ConnectivitySnapshotPayload(createdAt, EVENT_VERSION, connections),
        CONNECTIVITY_SNAPSHOT,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class ConnectivitySnapshotPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val connections: List<SimNetworkUtils.Connection>,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CONNECTIVITY_SNAPSHOT,
    ) : EventPayload() {
        override fun toSafeString(): String = connections.joinToString(", ") { "${it.type}: ${it.state}" }
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
