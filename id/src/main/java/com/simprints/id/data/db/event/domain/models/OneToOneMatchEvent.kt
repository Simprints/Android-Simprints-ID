package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventType.ONE_TO_ONE_MATCH
import java.util.*

@Keep
class OneToOneMatchEvent(
    createdAt: Long,
    endTime: Long,
    candidateId: String,
    result: MatchEntry?,
    sessionId: String = UUID.randomUUID().toString() //StopShip: to change in PAS-993
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(SessionIdLabel(sessionId)),
    OneToOneMatchPayload(createdAt, EVENT_VERSION, endTime, candidateId, result),
    ONE_TO_ONE_MATCH) {

    @Keep
    class OneToOneMatchPayload(
        createdAt: Long,
        eventVersion: Int,
        endTimeAt: Long,
        val candidateId: String,
        val result: MatchEntry?
    ) : EventPayload(ONE_TO_ONE_MATCH, eventVersion, createdAt, endTimeAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
