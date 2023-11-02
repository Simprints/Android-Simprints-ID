package com.simprints.infra.events.event.domain.models.callback

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ENROLMENT
import java.util.UUID

@Keep
data class EnrolmentCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentCallbackPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        guid: String,
        eventLabels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        eventLabels,
        EnrolmentCallbackPayload(createdAt, EVENT_VERSION, guid),
        CALLBACK_ENROLMENT
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class EnrolmentCallbackPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val guid: String,
        override val type: EventType = CALLBACK_ENROLMENT,
        override val endedAt: Long = 0,
    ) : EventPayload()


    companion object {
        const val EVENT_VERSION = 1
    }
}
