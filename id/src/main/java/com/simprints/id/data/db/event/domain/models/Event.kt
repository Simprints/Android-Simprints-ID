package com.simprints.id.data.db.event.domain.models

import com.beust.klaxon.TypeFor
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.local.EventAdapter

@TypeFor(field = "type", adapter = EventAdapter::class)
abstract class Event(open val id: String,
                     open var labels: EventLabels,
                     open val payload: EventPayload,
                     open val type: EventType = SESSION_CAPTURE) {

    companion object {
        const val DEFAULT_EVENT_VERSION = 0
    }
}
