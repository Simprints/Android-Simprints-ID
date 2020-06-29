package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class CompletionCheckEvent(
    creationTime: Long,
    completed: Boolean,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    CompletionCheckPayload(creationTime, completed)) {

    @Keep
    class CompletionCheckPayload(creationTime: Long,
                                 val completed: Boolean) : EventPayload(EventPayloadType.COMPLETION_CHECK, creationTime)
}
