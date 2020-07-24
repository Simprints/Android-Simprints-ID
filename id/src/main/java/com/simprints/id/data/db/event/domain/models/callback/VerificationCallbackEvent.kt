package com.simprints.id.data.db.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels

import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.CALLBACK_VERIFICATION
import java.util.*

@Keep
data class VerificationCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: VerificationCallbackPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        score: CallbackComparisonScore,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        VerificationCallbackPayload(createdAt, EVENT_VERSION, score),
        CALLBACK_VERIFICATION)

    @Keep
    data class VerificationCallbackPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val score: CallbackComparisonScore
    ) : EventPayload(CALLBACK_VERIFICATION, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
