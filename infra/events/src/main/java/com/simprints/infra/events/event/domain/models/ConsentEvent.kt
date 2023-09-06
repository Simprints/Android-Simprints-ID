package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizedString
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CONSENT
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
        CONSENT
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizedString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizedString>) = this // No tokenized fields


    @Keep
    data class ConsentPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        override var endedAt: Long,
        val consentType: Type,
        var result: Result,
        override val type: EventType = CONSENT
    ) : EventPayload() {

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
