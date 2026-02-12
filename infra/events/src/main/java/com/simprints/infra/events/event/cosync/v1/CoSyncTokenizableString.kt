package com.simprints.infra.events.event.cosync.v1

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * V1 external schema for tokenizable strings.
 *
 * Stable external contract decoupled from internal [TokenizableString].
 * Handles backward compatibility with plain strings and objects with/without className.
 */
@Keep
@Serializable(with = CoSyncTokenizableStringSerializer::class)
sealed class CoSyncTokenizableString {
    abstract val value: String

    data class Tokenized(override val value: String) : CoSyncTokenizableString()
    data class Raw(override val value: String) : CoSyncTokenizableString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is CoSyncTokenizableString && other.value == value
    }

    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value
}

fun TokenizableString.toCoSync(): CoSyncTokenizableString = when (this) {
    is TokenizableString.Tokenized -> CoSyncTokenizableString.Tokenized(value)
    is TokenizableString.Raw -> CoSyncTokenizableString.Raw(value)
}

fun CoSyncTokenizableString.toDomain(): TokenizableString = when (this) {
    is CoSyncTokenizableString.Tokenized -> TokenizableString.Tokenized(value)
    is CoSyncTokenizableString.Raw -> TokenizableString.Raw(value)
}

/**
 * Serializes [CoSyncTokenizableString] as `{ "className": "...", "value": "..." }`.
 * Deserializes plain strings, objects without className, and objects with className.
 */
internal object CoSyncTokenizableStringSerializer : KSerializer<CoSyncTokenizableString> {
    private const val TOKENIZED = "TokenizableString.Tokenized"
    private const val RAW = "TokenizableString.Raw"
    private const val FIELD_CLASS_NAME = "className"
    private const val FIELD_VALUE = "value"

    override val descriptor = buildClassSerialDescriptor("CoSyncTokenizableString") {
        element(FIELD_CLASS_NAME, PrimitiveSerialDescriptor(FIELD_CLASS_NAME, PrimitiveKind.STRING))
        element(FIELD_VALUE, PrimitiveSerialDescriptor(FIELD_VALUE, PrimitiveKind.STRING))
    }

    override fun serialize(encoder: Encoder, value: CoSyncTokenizableString) {
        require(encoder is JsonEncoder)
        val className = if (value is CoSyncTokenizableString.Tokenized) TOKENIZED else RAW
        encoder.encodeJsonElement(
            buildJsonObject {
                put(FIELD_CLASS_NAME, className)
                put(FIELD_VALUE, value.value)
            },
        )
    }

    override fun deserialize(decoder: Decoder): CoSyncTokenizableString {
        val jsonDecoder = decoder as? JsonDecoder ?: error("Only JSON is supported")
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> CoSyncTokenizableString.Raw(element.content)
            is JsonObject -> {
                val className = element[FIELD_CLASS_NAME]?.jsonPrimitive?.content.orEmpty()
                val value = element[FIELD_VALUE]?.jsonPrimitive?.content
                    ?: error("Missing 'value' field in CoSyncTokenizableString")
                if (className == TOKENIZED) CoSyncTokenizableString.Tokenized(value)
                else CoSyncTokenizableString.Raw(value)
            }
            else -> error("Unexpected JSON element for CoSyncTokenizableString")
        }
    }
}
