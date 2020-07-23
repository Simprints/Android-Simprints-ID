package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_ENROLMENT
import java.util.*

@Keep
class EnrolmentCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: EnrolmentCallbackPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(createdAt: Long,
                guid: String,
                sessionId: String = UUID.randomUUID().toString() /*StopShip: to change in PAS-993)*/) : this(
        UUID.randomUUID().toString(),
        mutableListOf(SessionIdLabel(sessionId)),
        EnrolmentCallbackPayload(createdAt, EVENT_VERSION, guid),
        CALLBACK_ENROLMENT)

    class EnrolmentCallbackPayload(createdAt: Long,
                                   eventVersion: Int,
                                   val guid: String) : EventPayload(CALLBACK_ENROLMENT, eventVersion, createdAt)


    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
