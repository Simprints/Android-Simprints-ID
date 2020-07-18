package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.beust.klaxon.TypeAdapter
import com.beust.klaxon.TypeFor
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.events.EventType.SESSION_CAPTURE
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
import kotlin.reflect.KClass

@Keep
@TypeFor(field = "type", adapter = EventTypeAdater::class)
abstract class Event(open val id: String,
            open val labels: MutableList<EventLabel>,
            open val payload: EventPayload,
            open val type: EventType = SESSION_CAPTURE) {

    companion object {
        const val DEFAULT_EVENT_VERSION = 0
    }
}

class EventTypeAdater: TypeAdapter<Event> {
    override fun classFor(type: Any): KClass<out Event> = when(type as String) {
        SESSION_CAPTURE.name -> SessionCaptureEvent::class
        else -> throw IllegalArgumentException("Unknown type: $type")
    }
}


fun Event.getSessionLabelIfExists(): SessionIdLabel? =
    labels.firstOrNull { it is SessionIdLabel } as SessionIdLabel?
