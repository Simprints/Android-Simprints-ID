package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_ENROLMENT
import com.simprints.infra.events.event.domain.models.EventType.Companion.CALLOUT_ENROLMENT_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(CALLOUT_ENROLMENT_KEY)
@Deprecated("Replaced by v3 in 2025.2.0")
data class EnrolmentCalloutEventV2(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: EnrolmentCalloutPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
        metadata: String?,
        id: String = UUID.randomUUID().toString(),
    ) : this(
        id,
        EnrolmentCalloutPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            metadata = metadata,
        ),
        CALLOUT_ENROLMENT,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = mapOf(
        TokenKeyType.AttendantId to payload.userId,
        TokenKeyType.ModuleId to payload.moduleId,
    )

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this.copy(
        payload = payload.copy(
            userId = map[TokenKeyType.AttendantId] ?: payload.userId,
            moduleId = map[TokenKeyType.ModuleId] ?: payload.moduleId,
        ),
    )

    @Keep
    @Serializable
    data class EnrolmentCalloutPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val projectId: String,
        val userId: TokenizableString,
        val moduleId: TokenizableString,
        val metadata: String?,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLOUT_ENROLMENT,
    ) : EventPayload() {
        override fun toSafeString(): String = "module: $moduleId, metadata: $metadata"
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
