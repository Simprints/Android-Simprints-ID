package com.simprints.id.data.db.event.domain.events

abstract class EventPayload(
    open val type: EventType,
    open val eventVersion: Int,
    open val createdAt: Long,
    open val endedAt: Long = 0
)
