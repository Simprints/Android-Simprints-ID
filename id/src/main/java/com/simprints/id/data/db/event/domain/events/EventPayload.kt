package com.simprints.id.data.db.event.domain.events

abstract class EventPayload(
    val type: EventPayloadType,
    val eventVersion: Int,
    val createdAt: Long
)
