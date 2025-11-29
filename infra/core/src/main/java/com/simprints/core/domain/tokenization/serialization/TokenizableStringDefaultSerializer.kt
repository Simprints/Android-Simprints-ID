package com.simprints.core.domain.tokenization.serialization

import com.simprints.core.domain.tokenization.TokenizableString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

object TokenizableStringDefaultSerializer :
    KSerializer<TokenizableString> {
    override val descriptor =
        PrimitiveSerialDescriptor("TokenizableString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TokenizableString {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("TokenizableString must be deserialized from JSON")

        val element = jsonDecoder.decodeJsonElement()

        return when (element) {
            is JsonPrimitive -> {
                // Plain string → Raw
                TokenizableString.Raw(element.content)
            }

            is JsonObject -> {
                val discriminator = element["className"]?.jsonPrimitive?.contentOrNull

                return when (discriminator) {
                    "TokenizableString.Tokenized" -> {
                        jsonDecoder.json.decodeFromJsonElement(TokenizableString.Tokenized.serializer(), element)
                    }

                    "TokenizableString.Raw" -> {
                        jsonDecoder.json.decodeFromJsonElement(TokenizableString.Raw.serializer(), element)
                    }

                    else -> {
                        // No or unknown discriminator: fallback to Raw with value field or error
                        val value = element["value"]?.jsonPrimitive?.content
                            ?: error("Missing 'value' field in TokenizableString JSON object")
                        TokenizableString.Raw(value)
                    }
                }
            }

            else -> {
                error("Unsupported TokenizableString format: $element")
            }
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: TokenizableString,
    ) {
        encoder.encodeString(value.value)
    }
}
