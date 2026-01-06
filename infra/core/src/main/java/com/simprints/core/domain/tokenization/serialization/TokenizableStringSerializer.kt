package com.simprints.core.domain.tokenization.serialization

import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

object TokenizableStringSerializer : KSerializer<TokenizableString> {
    private const val TOKENIZED = "TokenizableString.Tokenized"
    private const val RAW = "TokenizableString.Raw"
    private const val FIELD_CLASS_NAME = "className"
    private const val FIELD_VALUE = "value"

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("TokenizableString") {
            element(FIELD_CLASS_NAME, PrimitiveSerialDescriptor(FIELD_CLASS_NAME, PrimitiveKind.STRING))
            element(FIELD_VALUE, PrimitiveSerialDescriptor(FIELD_VALUE, PrimitiveKind.STRING))
        }

    override fun serialize(
        encoder: Encoder,
        value: TokenizableString,
    ) {
        require(encoder is JsonEncoder) {
            "TokenizableString can only be serialized to JSON"
        }

        val className = when (value) {
            is TokenizableString.Raw -> RAW
            is TokenizableString.Tokenized -> TOKENIZED
        }

        val jsonObject = JsonObject(
            mapOf(
                FIELD_CLASS_NAME to JsonPrimitive(className),
                FIELD_VALUE to JsonPrimitive(
                    when (value) {
                        is TokenizableString.Raw -> value.value
                        is TokenizableString.Tokenized -> value.value
                    },
                ),
            ),
        )

        encoder.encodeJsonElement(jsonObject)
    }

    override fun deserialize(decoder: Decoder): TokenizableString {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("This serializer only works with Json format")

        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> {
                // Plain string case: treat as Raw
                TokenizableString.Raw(element.content)
            }

            is JsonObject -> {
                val className = element["className"]?.jsonPrimitive?.content ?: ""
                val value = element["value"]?.jsonPrimitive?.content
                    ?: throw IllegalStateException("Missing 'value' field in TokenizableString")

                when (className) {
                    "TokenizableString.Tokenized" -> TokenizableString.Tokenized(value)
                    else -> TokenizableString.Raw(value)
                }
            }

            else -> {
                error("Unexpected JSON element for TokenizableString")
            }
        }
    }
}
