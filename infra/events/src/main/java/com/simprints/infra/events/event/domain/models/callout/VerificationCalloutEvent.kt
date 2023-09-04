package com.simprints.infra.events.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_VERIFICATION
import java.util.UUID

@Keep
data class VerificationCalloutEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: VerificationCalloutPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        projectId: String,
        userId: String,
        moduleId: String,
        verifyGuid: String,
        metadata: String,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        VerificationCalloutPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            verifyGuid = verifyGuid,
            metadata = metadata
        ),
        CALLOUT_VERIFICATION
    )

    override fun getTokenizedFields(): Map<TokenKeyType, String> = mapOf(
        TokenKeyType.AttendantId to payload.userId,
        TokenKeyType.ModuleId to payload.moduleId
    )

    override fun setTokenizedFields(map: Map<TokenKeyType, String>) = this.copy(
        payload = payload.copy(
            userId = map[TokenKeyType.AttendantId] ?: payload.userId,
            moduleId = map[TokenKeyType.ModuleId] ?: payload.moduleId
        )
    )

    @Keep
    data class VerificationCalloutPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val projectId: String,
        val userId: String,
        val moduleId: String,
        val verifyGuid: String,
        val metadata: String,
        override val type: EventType = CALLOUT_VERIFICATION,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }
}
