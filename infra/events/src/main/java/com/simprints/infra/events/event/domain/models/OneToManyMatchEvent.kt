package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.Companion.ONE_TO_MANY_MATCH_KEY
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_MANY_MATCH
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

@Keep
@Serializable
@SerialName(ONE_TO_MANY_MATCH_KEY)
data class OneToManyMatchEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: OneToManyMatchPayload,
    override val type: EventType,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    constructor(
        createdAt: Timestamp,
        endTime: Timestamp,
        pool: OneToManyMatchPayload.MatchPool,
        matcher: String,
        result: List<MatchEntry>?,
        probeBiometricReferenceId: String,
        batches: List<OneToManyMatchPayload.OneToManyBatch>? = null,
    ) : this(
        id = UUID.randomUUID().toString(),
        payload = OneToManyMatchPayload.OneToManyMatchPayloadV3(
            createdAt = createdAt,
            endedAt = endTime,
            pool = pool,
            matcher = matcher,
            result = result,
            probeBiometricReferenceId = probeBiometricReferenceId,
            batches = batches,
        ),
        type = ONE_TO_MANY_MATCH,
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this

    @Serializable(with = OneToManyMatchPayloadSerializer::class)
    sealed class OneToManyMatchPayload : EventPayload() {
        abstract override val type: EventType

        abstract val pool: MatchPool
        abstract val matcher: String
        abstract val result: List<MatchEntry>?

        @Keep
        @Serializable
        data class OneToManyMatchPayloadV2(
            override val createdAt: Timestamp,
            override val endedAt: Timestamp? = null,
            override val pool: MatchPool,
            override val matcher: String,
            override val result: List<MatchEntry>? = null,
            override val eventVersion: Int = EVENT_VERSION_WITHOUT_REFERENCE_ID,
            override val type: EventType = ONE_TO_MANY_MATCH,
        ) : OneToManyMatchPayload()

        @Keep
        @Serializable
        data class OneToManyMatchPayloadV3(
            val probeBiometricReferenceId: String,
            val batches: List<OneToManyBatch>? = null,
            override val createdAt: Timestamp,
            override val endedAt: Timestamp? = null,
            override val pool: MatchPool,
            override val matcher: String,
            override val result: List<MatchEntry>? = null,
            override val eventVersion: Int = EVENT_VERSION,
            override val type: EventType = ONE_TO_MANY_MATCH,
        ) : OneToManyMatchPayload()

        @Keep
        @Serializable
        data class MatchPool(
            val type: MatchPoolType,
            val count: Int,
        )

        @Keep
        @Serializable
        enum class MatchPoolType {
            USER,
            MODULE,
            PROJECT,
        }

        @Keep
        @Serializable
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

object OneToManyMatchPayloadSerializer : JsonContentPolymorphicSerializer<OneToManyMatchEvent.OneToManyMatchPayload>(
    OneToManyMatchEvent.OneToManyMatchPayload::class,
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<OneToManyMatchEvent.OneToManyMatchPayload> =
        when (val version = element.jsonObject["eventVersion"]?.jsonPrimitive?.intOrNull) {
            OneToManyMatchEvent.EVENT_VERSION_WITHOUT_REFERENCE_ID -> {
                OneToManyMatchEvent.OneToManyMatchPayload.OneToManyMatchPayloadV2
                    .serializer()
            }

            OneToManyMatchEvent.EVENT_VERSION -> {
                OneToManyMatchEvent.OneToManyMatchPayload.OneToManyMatchPayloadV3.serializer()
            }

            else -> {
                throw SerializationException("Unknown OneToManyMatchPayload eventVersion: $version")
            }
        }
}
