package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.REFUSAL
import java.util.UUID

@Keep
data class RefusalEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: RefusalPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {

    constructor(
        createdAt: Timestamp,
        endTime: Timestamp,
        reason: RefusalPayload.Answer,
        otherText: String,
    ) : this(
        UUID.randomUUID().toString(),
        RefusalPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            endedAt = endTime,
            reason = reason,
            otherText = otherText
        ),
        REFUSAL
    )


    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) =
        this // No tokenized fields

    @Keep
    data class RefusalPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        override var endedAt: Timestamp?,
        val reason: Answer,
        val otherText: String,
        override val type: EventType = REFUSAL,
    ) : EventPayload() {

        @Keep
        enum class Answer {
            REFUSED_RELIGION,
            REFUSED_DATA_CONCERNS,
            REFUSED_PERMISSION,
            SCANNER_NOT_WORKING,
            APP_NOT_WORKING,
            REFUSED_NOT_PRESENT,
            REFUSED_YOUNG,
            WRONG_AGE_GROUP_SELECTED,
            UNCOOPERATIVE_CHILD,
            OTHER
        }
    }

    companion object {

        const val EVENT_VERSION = 2
    }
}
