package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_V2
import java.util.UUID

@Keep
@Deprecated("Replaced by v4 in 2025.1.0")
data class EnrolmentEventV2(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: EnrolmentPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        subjectId: String,
        projectId: String,
        moduleId: TokenizableString,
        attendantId: TokenizableString,
        personCreationEventId: String,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentPayload(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            subjectId = subjectId,
            projectId = projectId,
            moduleId = moduleId,
            attendantId = attendantId,
            personCreationEventId = personCreationEventId,
        ),
        ENROLMENT_V2,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = mapOf(
        TokenKeyType.AttendantId to payload.attendantId,
        TokenKeyType.ModuleId to payload.moduleId,
    )

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this.copy(
        payload = payload.copy(
            attendantId = map[TokenKeyType.AttendantId] ?: payload.attendantId,
            moduleId = map[TokenKeyType.ModuleId] ?: payload.moduleId,
        ),
    )

    @Keep
    data class EnrolmentPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: TokenizableString,
        val attendantId: TokenizableString,
        val personCreationEventId: String,
        override val endedAt: Timestamp? = null,
        override val type: EventType = ENROLMENT_V2,
    ) : EventPayload() {
        override fun toSafeString(): String = "subject ID: $subjectId, module ID: $moduleId"
    }

    companion object {
        const val EVENT_VERSION = 3
    }
}
