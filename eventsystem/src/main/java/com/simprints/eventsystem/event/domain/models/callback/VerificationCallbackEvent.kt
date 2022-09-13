package com.simprints.eventsystem.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.CALLBACK_VERIFICATION
import java.util.*

@Keep
data class VerificationCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: VerificationCallbackPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        score: CallbackComparisonScore,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        VerificationCallbackPayload(createdAt, EVENT_VERSION, score),
        CALLBACK_VERIFICATION)

    @Keep
    data class VerificationCallbackPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val score: CallbackComparisonScore,
        override val type: EventType = CALLBACK_VERIFICATION,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
