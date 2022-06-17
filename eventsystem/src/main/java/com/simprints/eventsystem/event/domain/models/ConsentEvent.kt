package com.simprints.eventsystem.event.domain.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.EventType.CONSENT
import java.util.UUID

@Keep
data class ConsentEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ConsentPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        endTime: Long,
        consentType: ConsentPayload.Type,
        result: ConsentPayload.Result,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ConsentPayload(createdAt, EVENT_VERSION, endTime, consentType, result),
        CONSENT)


    @Keep
    data class ConsentPayload(override val createdAt: Long,
                              override val eventVersion: Int,
                              override var endedAt: Long,
                              val consentType: Type,
                              var result: Result,
                              override val type: EventType = CONSENT) : EventPayload() {

        @Keep
        enum class Type {
            INDIVIDUAL, PARENTAL
        }

        @Keep
        enum class Result {
            ACCEPTED, DECLINED, NO_RESPONSE
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
