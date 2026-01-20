package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_LAST_BIOMETRICS
import com.simprints.infra.events.event.domain.models.EventType.Companion.CALLOUT_LAST_BIOMETRICS_KEY
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(CALLOUT_LAST_BIOMETRICS_KEY)
@Deprecated("Replaced by v3 in 2025.2.0")
data class EnrolmentLastBiometricsCalloutEventV2(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: EnrolmentLastBiometricsCalloutPayload,
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
        sessionId: String,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentLastBiometricsCalloutPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            metadata = metadata,
            sessionId = sessionId,
        ),
        CALLOUT_LAST_BIOMETRICS,
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
    data class EnrolmentLastBiometricsCalloutPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val projectId: String,
        val userId: TokenizableString,
        val moduleId: TokenizableString,
        val metadata: String?,
        val sessionId: String,
        override val endedAt: Timestamp? = null,
        override val type: EventType = CALLOUT_LAST_BIOMETRICS,
    ) : EventPayload() {
        override fun toSafeString(): String = "metadata: $metadata, session ID: $sessionId"
    }

    companion object {
        const val EVENT_VERSION = 2
    }
}
