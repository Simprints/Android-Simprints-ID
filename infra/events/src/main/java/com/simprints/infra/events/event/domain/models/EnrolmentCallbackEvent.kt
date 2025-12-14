package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CALLBACK_ENROLMENT
import com.simprints.infra.events.event.domain.models.EventType.Companion.CALLBACK_ENROLMENT_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(CALLBACK_ENROLMENT_KEY)
data class EnrolmentCallbackEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: EnrolmentCallbackPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        guid: String,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentCallbackPayload(createdAt, EVENT_VERSION, guid),
        CALLBACK_ENROLMENT,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    @Serializable
    data class EnrolmentCallbackPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val guid: String,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLBACK_ENROLMENT,
    ) : EventPayload() {
        override fun toSafeString(): String = "guid: $guid"
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
