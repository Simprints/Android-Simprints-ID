package com.simprints.id.data.db.event.domain.events

import androidx.annotation.Keep
import java.util.*

@Keep
class CompletionCheckEvent(
    startTime: Long,
    completed: Boolean,
    sessionId: String = UUID.randomUUID().toString(), //StopShip: to change in PAS-993
    sessionStartTime: Long = 0 //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    listOf(EventLabel.SessionId(sessionId)),
    CompletionCheckPayload(startTime, startTime - sessionStartTime, completed)) {

    @Keep
    class CompletionCheckPayload(startTime: Long,
                                 relativeStartTime: Long,
                                 val completed: Boolean) : EventPayload(EventPayloadType.COMPLETION_CHECK, startTime, relativeStartTime)
}
