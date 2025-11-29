package com.simprints.infra.events.event.domain.models

import com.simprints.core.tools.time.Timestamp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
sealed class EventPayload {
    @OptIn(ExperimentalSerializationApi::class)
    abstract val type: EventType
    abstract val eventVersion: Int
    abstract val createdAt: Timestamp
    abstract val endedAt: Timestamp?

    open fun toSafeString(): String = ""
}
