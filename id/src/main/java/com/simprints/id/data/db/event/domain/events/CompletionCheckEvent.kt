package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EventLabel.SessionIdLabel
import java.util.*

@Keep
class CompletionCheckEvent(
    createdAt: Long,
    completed: Boolean,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    CompletionCheckPayload(createdAt, DEFAULT_EVENT_VERSION, completed)) {

    @Keep
    class CompletionCheckPayload(createdAt: Long,
                                 eventVersion: Int,
                                 val completed: Boolean) : EventPayload(EventType.COMPLETION_CHECK, eventVersion, createdAt)
}
