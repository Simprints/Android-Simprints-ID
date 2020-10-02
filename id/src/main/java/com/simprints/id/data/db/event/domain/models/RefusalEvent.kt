package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.EventType.REFUSAL
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

@Keep
data class RefusalEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: RefusalPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        endTime: Long,
        reason: RefusalPayload.Answer,
        otherText: String,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        RefusalPayload(createdAt, EVENT_VERSION, endTime, reason, otherText),
        REFUSAL)


    @Keep
    data class RefusalPayload(override val createdAt: Long,
                              override val eventVersion: Int,
                              override var endedAt: Long,
                              val reason: Answer,
                              val otherText: String,
                              override val type: EventType = REFUSAL) : EventPayload() {

        @Keep
        enum class Answer {
            REFUSED_RELIGION,
            REFUSED_DATA_CONCERNS,
            REFUSED_PERMISSION,
            SCANNER_NOT_WORKING,
            APP_NOT_WORKING,
            REFUSED_NOT_PRESENT,
            REFUSED_YOUNG,
            OTHER
        }
    }

    companion object {
        const val EVENT_VERSION = 1
    }
}
