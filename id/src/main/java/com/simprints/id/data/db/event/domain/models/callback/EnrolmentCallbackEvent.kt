package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_ENROLMENT
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

@Keep
data class EnrolmentCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentCallbackPayload,
    override val type: EventType
) : Event() {

    constructor(createdAt: Long,
                guid: String,
                eventLabels: EventLabels = EventLabels() /*StopShip: to change in PAS-993)*/) : this(
        UUID.randomUUID().toString(),
        eventLabels, //StopShip: to change in PAS-993
        EnrolmentCallbackPayload(createdAt, EVENT_VERSION, guid),
        CALLBACK_ENROLMENT)

    data class EnrolmentCallbackPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val guid: String,
        override val type: EventType = CALLBACK_ENROLMENT,
        override val endedAt: Long = 0) : EventPayload()


    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
