package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_CONFIRMATION
import java.util.*

@Keep
class ConfirmationCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels = EventLabels(),
    override val payload: ConfirmationCallbackPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        identificationOutcome: Boolean,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ConfirmationCallbackPayload(createdAt, EVENT_VERSION, identificationOutcome),
        CALLBACK_CONFIRMATION)

    class ConfirmationCallbackPayload(createdAt: Long,
                                      eventVersion: Int,
                                      val identificationOutcome: Boolean)
        : EventPayload(CALLBACK_CONFIRMATION, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
