package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_ENROLMENT
import java.util.*

@Keep
class EnrolmentCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentCallbackPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(createdAt: Long,
                guid: String,
                eventLabels: EventLabels = EventLabels() /*StopShip: to change in PAS-993)*/) : this(
        UUID.randomUUID().toString(),
        eventLabels, //StopShip: to change in PAS-993
        EnrolmentCallbackPayload(createdAt, EVENT_VERSION, guid),
        CALLBACK_ENROLMENT)

    class EnrolmentCallbackPayload(createdAt: Long,
                                   eventVersion: Int,
                                   val guid: String) : EventPayload(CALLBACK_ENROLMENT, eventVersion, createdAt)


    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
