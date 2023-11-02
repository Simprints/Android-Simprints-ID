package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_V1
import java.util.*

@Keep
@Deprecated("Used only for the migration before 2021.1.0")
data class EnrolmentEventV1(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        personId: String,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        EnrolmentPayload(createdAt, EVENT_VERSION, personId),
        ENROLMENT_V1)

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class EnrolmentPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val personId: String,
        override val type: EventType = ENROLMENT_V1,
        override val endedAt: Long = 0) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
