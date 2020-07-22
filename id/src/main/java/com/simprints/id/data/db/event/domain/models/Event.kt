package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.beust.klaxon.TypeFor
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.local.EventAdapter

@Keep
@TypeFor(field = "type", adapter = EventAdapter::class)
abstract class Event(open val id: String,
                     open val labels: MutableList<EventLabel>,
                     open val payload: EventPayload,
                     open val type: EventType = SESSION_CAPTURE) {

    companion object {
        const val DEFAULT_EVENT_VERSION = 0
    }
}


fun Event.getSessionLabelIfExists(): SessionIdLabel? =
    labels.firstOrNull { it is SessionIdLabel } as SessionIdLabel?
