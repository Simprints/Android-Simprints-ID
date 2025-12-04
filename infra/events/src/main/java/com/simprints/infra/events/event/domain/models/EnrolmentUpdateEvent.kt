package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.Companion.ENROLMENT_UPDATE_KEY
import com.simprints.infra.events.event.domain.models.EventType.ENROLMENT_UPDATE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@SerialName(ENROLMENT_UPDATE_KEY)
@ExcludedFromGeneratedTestCoverageReports("Data struct")
data class EnrolmentUpdateEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: EnrolmentUpdatePayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        subjectId: String,
        externalCredentialIdsToAdd: List<String>,
    ) : this(
        UUID.randomUUID().toString(),
        EnrolmentUpdatePayload(createdAt, EVENT_VERSION, subjectId, externalCredentialIdsToAdd),
        ENROLMENT_UPDATE,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    @Serializable
    @ExcludedFromGeneratedTestCoverageReports("Data struct")
    data class EnrolmentUpdatePayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        val subjectId: String,
        val externalCredentialIdsToAdd: List<String>,
        override val endedAt: Timestamp? = null,
        override val type: EventType = ENROLMENT_UPDATE,
    ) : EventPayload() {
        override fun toSafeString(): String = "subjectId: $subjectId, credentials: $externalCredentialIdsToAdd"
    }

    companion object Companion {
        const val EVENT_VERSION = 0
    }
}
