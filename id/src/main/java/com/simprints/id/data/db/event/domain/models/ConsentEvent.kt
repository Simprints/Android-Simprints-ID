package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.CONSENT
import java.util.*

@Keep
class ConsentEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ConsentPayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(
        createdAt: Long,
        endTime: Long,
        consentType: ConsentPayload.Type,
        result: ConsentPayload.Result,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ConsentPayload(createdAt, EVENT_VERSION, endTime, consentType, result),
        CONSENT)


    @Keep
    class ConsentPayload(override val createdAt: Long,
                         override val eventVersion: Int,
                         override val endedAt: Long,
                         val consentType: Type,
                         var result: Result) : EventPayload(CONSENT, eventVersion, createdAt, endedAt) {

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
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
