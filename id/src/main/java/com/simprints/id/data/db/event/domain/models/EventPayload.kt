package com.simprints.id.data.db.event.domain.models

import com.beust.klaxon.TypeFor
import com.simprints.id.data.db.event.local.EventAdapter

@TypeFor(field = "type", adapter = EventAdapter::class)
abstract class EventPayload(
    open val type: EventType,
    open val eventVersion: Int,
    open val createdAt: Long,
    open var endedAt: Long = 0
)
