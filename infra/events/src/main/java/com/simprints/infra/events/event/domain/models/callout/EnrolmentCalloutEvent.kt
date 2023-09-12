package com.simprints.infra.events.event.domain.models.callout

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.EventLabels
import com.simprints.infra.events.event.domain.models.EventPayload
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.EventType.CALLOUT_ENROLMENT
import java.util.UUID

@Keep
data class EnrolmentCalloutEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentCalloutPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        projectId: String,
        userId: TokenizableString,
        moduleId: TokenizableString,
        metadata: String?,
        labels: EventLabels = EventLabels(),
        id: String = UUID.randomUUID().toString()
    ) : this(
        id,
        labels,
        EnrolmentCalloutPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            metadata = metadata
        ),
        CALLOUT_ENROLMENT
    )

    override fun getTokenizedFields(): Map<TokenKeyType, TokenizableString> = mapOf(
        TokenKeyType.AttendantId to payload.userId,
        TokenKeyType.ModuleId to payload.moduleId
    )

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this.copy(
        payload = payload.copy(
            userId = map[TokenKeyType.AttendantId] ?: payload.userId,
            moduleId = map[TokenKeyType.ModuleId] ?: payload.moduleId
        )
    )

    @Keep
    data class EnrolmentCalloutPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val projectId: String,
        val userId: TokenizableString,
        val moduleId: TokenizableString,
        val metadata: String?,
        override val type: EventType = CALLOUT_ENROLMENT,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 1
    }

}
