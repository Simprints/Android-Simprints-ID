package com.simprints.id.data.db.event.domain.events

abstract class EventPayload(
    val type: EventPayloadType,
    val creationTime: Long,
    val uploadTime: Long? = null
)
