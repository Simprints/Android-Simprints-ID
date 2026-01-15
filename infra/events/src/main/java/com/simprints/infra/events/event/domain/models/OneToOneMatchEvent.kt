
package com.simprints.infra.events.event.domain.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.EventType.Companion.ONE_TO_ONE_MATCH_KEY
import com.simprints.infra.events.event.domain.models.EventType.ONE_TO_ONE_MATCH
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
@SerialName(ONE_TO_ONE_MATCH_KEY)
data class OneToOneMatchEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: OneToOneMatchPayload,
    override var scopeId: String? = null,
    override var projectId: String? = null,
) : Event() {
    override val type: EventType = ONE_TO_ONE_MATCH

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
            endedAt = endTime,
            candidateId = candidateId,
            matcher = matcher,
            result = result,
            fingerComparisonStrategy = fingerComparisonStrategy,
            probeBiometricReferenceId = probeBiometricReferenceId,
        ),
    )

    override fun getTokenizableFields(): Map<TokenKeyType, TokenizableString> = emptyMap()

    override fun setTokenizedFields(map: Map<TokenKeyType, TokenizableString>) = this // No tokenized fields

    @Keep
    @Serializable(with = OneToOneMatchPayloadSerializer::class) // Now this works!
    sealed class OneToOneMatchPayload : EventPayload() {
        abstract override val type: EventType // This caused the conflict, now it's just a property

        abstract val candidateId: String
        abstract val matcher: String
        abstract val result: MatchEntry?
        abstract val fingerComparisonStrategy: FingerComparisonStrategy?

        override fun toSafeString(): String = "matcher: $matcher, candidate ID: $candidateId..."

        @Keep
        @Serializable
        data class OneToOneMatchPayloadV3(
            override val createdAt: Timestamp,
            override var endedAt: Timestamp?,
            override val candidateId: String,
            override val matcher: String,
            override val result: MatchEntry?,
            override val fingerComparisonStrategy: FingerComparisonStrategy?,
            override val eventVersion: Int = VERSION_WITHOUT_REFERENCE_ID,
            override val type: EventType = ONE_TO_ONE_MATCH,
        ) : OneToOneMatchPayload() // Parent constructor is empty

        @Keep
        @Serializable
        data class OneToOneMatchPayloadV4(
            override val createdAt: Timestamp,
            override var endedAt: Timestamp?,
            override val candidateId: String,
            override val matcher: String,
            override val result: MatchEntry?,
            override val fingerComparisonStrategy: FingerComparisonStrategy?,
            val probeBiometricReferenceId: String,
            // Overridden here
            override val eventVersion: Int = EVENT_VERSION,
            override val type: EventType = ONE_TO_ONE_MATCH,
        ) : OneToOneMatchPayload() // Parent constructor is empty
    }

    companion object {
        const val VERSION_WITHOUT_REFERENCE_ID = 3
        const val EVENT_VERSION = 4
    }
}

object OneToOneMatchPayloadSerializer : JsonContentPolymorphicSerializer<OneToOneMatchEvent.OneToOneMatchPayload>(
    OneToOneMatchEvent.OneToOneMatchPayload::class,
) {
    // Uses 'eventVersion' to decide which class to create
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<OneToOneMatchEvent.OneToOneMatchPayload> =
        when (val version = element.jsonObject["eventVersion"]?.jsonPrimitive?.intOrNull) {
            OneToOneMatchEvent.VERSION_WITHOUT_REFERENCE_ID -> OneToOneMatchEvent.OneToOneMatchPayload.OneToOneMatchPayloadV3.serializer()
            OneToOneMatchEvent.EVENT_VERSION -> OneToOneMatchEvent.OneToOneMatchPayload.OneToOneMatchPayloadV4.serializer()
            else -> throw SerializationException("Unknown OneToOneMatchPayload eventVersion: $version")
        }
}
