package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_ONE_MATCH
import java.util.UUID

@Keep
data class OneToOneMatchEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: OneToOneMatchPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        endTime: Timestamp,
        candidateId: String,
        matcher: String,
        result: MatchEntry?,
        fingerComparisonStrategy: FingerComparisonStrategy?,
        probeBiometricReferenceId: String,
    ) : this(
        id = UUID.randomUUID().toString(),
        payload = OneToOneMatchPayload.OneToOneMatchPayloadV4(
            createdAt = createdAt,
            eventVersion = EVENT_VERSION,
            endedAt = endTime,
            candidateId = candidateId,
            matcher = matcher,
            result = result,
            fingerComparisonStrategy = fingerComparisonStrategy,
            probeBiometricReferenceId = probeBiometricReferenceId,
        ),
        type = ONE_TO_ONE_MATCH,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventVersion",
        visible = true,
    )
    @JsonSubTypes(
        JsonSubTypes.Type(
            value = OneToOneMatchPayload.OneToOneMatchPayloadV3::class,
            name = VERSION_WITHOUT_REFERENCE_ID.toString(),
        ),
        JsonSubTypes.Type(
            value = OneToOneMatchPayload.OneToOneMatchPayloadV4::class,
            name = EVENT_VERSION.toString(),
        ),
    )
    @Keep
    sealed class OneToOneMatchPayload(
        override val createdAt: Timestamp,
        override val eventVersion: Int,
        override var endedAt: Timestamp?,
        open val candidateId: String,
        open val matcher: String,
        open val result: MatchEntry?,
        open val fingerComparisonStrategy: FingerComparisonStrategy?,
        override val type: EventType = ONE_TO_ONE_MATCH,
    ) : EventPayload() {
        override fun toSafeString(): String = "matcher: $matcher, candidate ID: $candidateId, " +
            "result: ${result?.score}, finger strategy: $fingerComparisonStrategy"

        @Keep
        data class OneToOneMatchPayloadV3(
            override val createdAt: Timestamp,
            override val eventVersion: Int,
            override var endedAt: Timestamp?,
            override val candidateId: String,
            override val matcher: String,
            override val result: MatchEntry?,
            override val fingerComparisonStrategy: FingerComparisonStrategy?,
        ) : OneToOneMatchPayload(createdAt, eventVersion, endedAt, candidateId, matcher, result, fingerComparisonStrategy)

        @Keep
        data class OneToOneMatchPayloadV4(
            override val createdAt: Timestamp,
            override val eventVersion: Int,
            override var endedAt: Timestamp?,
            override val candidateId: String,
            override val matcher: String,
            override val result: MatchEntry?,
            override val fingerComparisonStrategy: FingerComparisonStrategy?,
            val probeBiometricReferenceId: String,
        ) : OneToOneMatchPayload(createdAt, eventVersion, endedAt, candidateId, matcher, result, fingerComparisonStrategy)
    }

    companion object {
        const val VERSION_WITHOUT_REFERENCE_ID = 3
        const val EVENT_VERSION = 4
    }
}
