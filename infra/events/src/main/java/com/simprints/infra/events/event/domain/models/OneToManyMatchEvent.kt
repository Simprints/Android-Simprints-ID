package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_MANY_MATCH
import java.util.UUID

@Keep
data class OneToManyMatchEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: OneToManyMatchPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        startTime: Timestamp,
        endTime: Timestamp,
        pool: OneToManyMatchPayload.MatchPool,
        matcher: String,
        result: List<MatchEntry>?,
        probeBiometricReferenceId: String,
        batches: List<OneToManyMatchPayload.OneToManyBatch>? = null,
    ) : this(
        id = UUID.randomUUID().toString(),
        payload = OneToManyMatchPayload.OneToManyMatchPayloadV3(
            startTime = startTime,
            eventVersion = EVENT_VERSION,
            endTime = endTime,
            pool = pool,
            matcher = matcher,
            result = result,
            probeBiometricReferenceId = probeBiometricReferenceId,
            batches = batches,
        ),
        type = ONE_TO_MANY_MATCH,
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
            value = OneToManyMatchPayload.OneToManyMatchPayloadV2::class,
            name = EVENT_VERSION_WITHOUT_REFERENCE_ID.toString(),
        ),
        JsonSubTypes.Type(
            value = OneToManyMatchPayload.OneToManyMatchPayloadV3::class,
            name = EVENT_VERSION.toString(),
        ),
    )
    @Keep
    sealed class OneToManyMatchPayload(
        override val startTime: Timestamp,
        override val eventVersion: Int,
        override val endTime: Timestamp?,
        open val pool: MatchPool,
        open val matcher: String,
        open val result: List<MatchEntry>?,
        override val type: EventType = ONE_TO_MANY_MATCH,
    ) : EventPayload() {
        override fun toSafeString(): String = "matcher: $matcher, pool: ${pool.type}, size: ${pool.count}, results: ${result?.size}"

        @Keep
        data class OneToManyMatchPayloadV2(
            override val startTime: Timestamp,
            override val eventVersion: Int,
            override val endTime: Timestamp?,
            override val pool: MatchPool,
            override val matcher: String,
            override val result: List<MatchEntry>?,
        ) : OneToManyMatchPayload(startTime, eventVersion, endTime, pool, matcher, result)

        @Keep
        data class OneToManyMatchPayloadV3(
            override val startTime: Timestamp,
            override val eventVersion: Int,
            override val endTime: Timestamp?,
            override val pool: MatchPool,
            override val matcher: String,
            override val result: List<MatchEntry>?,
            val probeBiometricReferenceId: String,
            val batches: List<OneToManyBatch>? = null,
        ) : OneToManyMatchPayload(startTime, eventVersion, endTime, pool, matcher, result)

        @Keep
        data class MatchPool(
            val type: MatchPoolType,
            val count: Int,
        )

        @Keep
        enum class MatchPoolType {
            USER,
            MODULE,
            PROJECT,
        }

        @Keep
        data class OneToManyBatch(
            val loadingStartTime: Timestamp,
            val loadingEndTime: Timestamp,
            val comparingStartTime: Timestamp,
            val comparingEndTime: Timestamp,
            val count: Int,
        )
    }

    companion object {
        const val EVENT_VERSION_WITHOUT_REFERENCE_ID = 2
        const val EVENT_VERSION = 3
    }
}
