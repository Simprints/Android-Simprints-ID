package com.simprints.infra.events.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_LAST_BIOMETRICS_V3
import java.util.UUID

@Keep
data class EnrolmentLastBiometricsCalloutEventV3(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: EnrolmentLastBiometricsCalloutPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
        metadata: String?,
        sessionId: String,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentLastBiometricsCalloutPayload(
            startTime = startTime,
            eventVersion = EVENT_VERSION,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            metadata = metadata,
            sessionId = sessionId,
        ),
        CALLOUT_LAST_BIOMETRICS_V3,
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
    data class EnrolmentLastBiometricsCalloutPayload(
        override val startTime: Timestamp,
        override val eventVersion: Int,
        val projectId: String,
        val userId: TokenizableString,
        val moduleId: TokenizableString,
        val metadata: String?,
        val sessionId: String,
        override val endTime: Timestamp? = null,
        override val type: EventType = CALLOUT_LAST_BIOMETRICS_V3,
    ) : EventPayload() {
        override fun toSafeString(): String = "metadata: $metadata, session ID: $sessionId"
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
