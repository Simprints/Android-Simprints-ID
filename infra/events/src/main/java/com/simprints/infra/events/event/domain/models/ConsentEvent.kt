package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CONSENT
import com.simprints.infra.events.event.domain.models.EventType.Companion.CONSENT_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(CONSENT_KEY)
data class ConsentEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ConsentPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        endTime: Timestamp,
        consentType: ConsentPayload.Type,
        result: ConsentPayload.Result,
    ) : this(
        UUID.randomUUID().toString(),
        ConsentPayload(createdAt, EVENT_VERSION, endTime, consentType, result),
        CONSENT,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    @Serializable
    data class ConsentPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        override val endedAt: Timestamp?,
        val consentType: Type,
        var result: Result,
        override val type: EventType = CONSENT,
    ) : EventPayload() {
        override fun toSafeString(): String = "consent: $consentType, result: $result"

        @Keep
        @Serializable
        enum class Type {
            INDIVIDUAL,
            PARENTAL,
        }

        @Keep
        @Serializable
        enum class Result {
            ACCEPTED,
            DECLINED,
            NO_RESPONSE,
        }
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
