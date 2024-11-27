package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_V1
import java.util.UUID

@Keep
@Deprecated("Used only for the migration before 2021.1.0")
data class EnrolmentEventV1(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: EnrolmentPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {

    constructor(
        createdAt: Timestamp,
        personId: String,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentPayload(createdAt, EVENT_VERSION, personId),
        ENROLMENT_V1,
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) =
        this // No tokenized fields

    @Keep
    data class EnrolmentPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val personId: String,
        override val endedAt: Timestamp? = null,
        override val type: EventType = ENROLMENT_V1,
    ) : EventPayload() {

        override fun toSafeString(): String = "person ID: $personId"
    }

    companion object {

        const val EVENT_VERSION = 2
    }
}
