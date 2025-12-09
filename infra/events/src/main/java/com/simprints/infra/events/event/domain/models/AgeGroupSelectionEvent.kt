package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.AGE_GROUP_SELECTION
import java.util.UUID

@Keep
data class AgeGroupSelectionEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: AgeGroupSelectionPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
        subjectAgeGroup: AgeGroup,
    ) : this(
        UUID.randomUUID().toString(),
        AgeGroupSelectionPayload(startTime, EVENT_VERSION, endTime, subjectAgeGroup),
        AGE_GROUP_SELECTION,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    data class AgeGroupSelectionPayload(
        override val startTime: Timestamp,
        override val eventVersion: Int,
        override val endTime: Timestamp?,
        val subjectAgeGroup: AgeGroup,
        override val type: EventType = AGE_GROUP_SELECTION,
    ) : EventPayload() {
        override fun toSafeString(): String = "age group: [${subjectAgeGroup.startInclusive}, ${subjectAgeGroup.endExclusive})"
    }

    @Keep
    data class AgeGroup(
        val startInclusive: Int,
        val endExclusive: Int?,
    )

    companion object {
        const val EVENT_VERSION = 1
    }
}
