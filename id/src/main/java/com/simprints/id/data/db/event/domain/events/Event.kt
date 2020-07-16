package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel

@Keep
abstract class Event(val id: String,
                     val labels: MutableList<EventLabel>,
                     val payload: EventPayload) {

    companion object {
        const val DEFAULT_EVENT_VERSION = 0
    }
}

fun Event.getSessionLabelIfExists(): SessionIdLabel? =
    labels.firstOrNull { it is SessionIdLabel } as SessionIdLabel?
