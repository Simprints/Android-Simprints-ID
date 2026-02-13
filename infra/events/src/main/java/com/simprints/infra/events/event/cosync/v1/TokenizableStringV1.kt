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
@Serializable(with = TokenizableStringV1Serializer::class)
sealed class TokenizableStringV1 {
    abstract val value: String

    data class Tokenized(override val value: String) : TokenizableStringV1()
    data class Raw(override val value: String) : TokenizableStringV1()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is TokenizableStringV1 && other.value == value
    }

    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value
}

fun TokenizableString.toCoSyncV1(): TokenizableStringV1 = when (this) {
    is TokenizableString.Tokenized -> TokenizableStringV1.Tokenized(value)
    is TokenizableString.Raw -> TokenizableStringV1.Raw(value)
}

fun TokenizableStringV1.toDomain(): TokenizableString = when (this) {
    is TokenizableStringV1.Tokenized -> TokenizableString.Tokenized(value)
    is TokenizableStringV1.Raw -> TokenizableString.Raw(value)
}

/**
 * Serializes [TokenizableStringV1] as `{ "className": "...", "value": "..." }`.
 * Deserializes plain strings, objects without className, and objects with className.
 */
internal object TokenizableStringV1Serializer : KSerializer<TokenizableStringV1> {
    private const val TOKENIZED = "TokenizableString.Tokenized"
    private const val RAW = "TokenizableString.Raw"
    private const val FIELD_CLASS_NAME = "className"
    private const val FIELD_VALUE = "value"

    override val descriptor = buildClassSerialDescriptor("TokenizableStringV1") {
        element(FIELD_CLASS_NAME, PrimitiveSerialDescriptor(FIELD_CLASS_NAME, PrimitiveKind.STRING))
        element(FIELD_VALUE, PrimitiveSerialDescriptor(FIELD_VALUE, PrimitiveKind.STRING))
    }

    override fun serialize(encoder: Encoder, value: TokenizableStringV1) {
        require(encoder is JsonEncoder)
        val className = if (value is TokenizableStringV1.Tokenized) TOKENIZED else RAW
        encoder.encodeJsonElement(
            buildJsonObject {
                put(FIELD_CLASS_NAME, className)
                put(FIELD_VALUE, value.value)
            },
        )
    }

    override fun deserialize(decoder: Decoder): TokenizableStringV1 {
        val jsonDecoder = decoder as? JsonDecoder ?: error("Only JSON is supported")
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> TokenizableStringV1.Raw(element.content)
            is JsonObject -> {
                val className = element[FIELD_CLASS_NAME]?.jsonPrimitive?.content.orEmpty()
                val value = element[FIELD_VALUE]?.jsonPrimitive?.content
                    ?: error("Missing 'value' field in TokenizableStringV1")
                if (className == TOKENIZED) TokenizableStringV1.Tokenized(value)
                else TokenizableStringV1.Raw(value)
            }
            else -> error("Unexpected JSON element for TokenizableStringV1")
        }
    }
}
