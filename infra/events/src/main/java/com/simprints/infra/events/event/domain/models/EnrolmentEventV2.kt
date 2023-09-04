package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_V2
import java.util.UUID

@Keep
data class EnrolmentEventV2(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        subjectId: String,
        projectId: String,
        moduleId: String,
        attendantId: String,
        personCreationEventId: String,
        labels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        EnrolmentPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            subjectId = subjectId,
            projectId = projectId,
            moduleId = moduleId,
            attendantId = attendantId,
            personCreationEventId = personCreationEventId
        ),
        ENROLMENT_V2
    )

    override fun getTokenizedFields(): Map<TokenKeyType, String> = mapOf(
        TokenKeyType.AttendantId to payload.attendantId,
        TokenKeyType.ModuleId to payload.moduleId
    )

    override fun setTokenizedFields(map: Map<TokenKeyType, String>) = this.copy(
        payload = payload.copy(
            attendantId = map[TokenKeyType.AttendantId] ?: payload.attendantId,
            moduleId = map[TokenKeyType.ModuleId] ?: payload.moduleId
        )
    )


    @Keep
    data class EnrolmentPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val personCreationEventId: String,
        override val type: EventType = ENROLMENT_V2,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {
        const val EVENT_VERSION = 2
    }
}
