package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabel
import com.simprints.id.data.db.event.domain.models.EventLabel.SessionIdLabel
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_CONFIRMATION
import java.util.*

@Keep
class ConfirmationCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val labels: MutableList<EventLabel>,
    override val payload: ConfirmationCallbackPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        identificationOutcome: Boolean,
        sessionId: String = UUID.randomUUID().toString() //STOPSHIP: To remove
    ) : this(
        UUID.randomUUID().toString(),
        mutableListOf(SessionIdLabel(sessionId)),
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
