package com.simprints.infra.events.event.cosync.v1

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

private const val TOKENIZED = "TokenizableString.Tokenized"
private const val RAW = "TokenizableString.Raw"
private const val FIELD_CLASS_NAME = "className"
private const val FIELD_VALUE = "value"

/**
 * JSON serializer for [TokenizableStringV1] that creates an explicit specification
 * of the child class being used.
 *
 * Examples:
 * TokenizableStringV1.Raw(value = "person")
 *     -> { "className": "TokenizableString.Raw", "value": "person" }
 *
 * TokenizableStringV1.Tokenized(value = "eq2Efc98d")
 *     -> { "className": "TokenizableString.Tokenized", "value": "eq2Efc98d" }
 */
class TokenizableStringV1Serializer : StdSerializer<TokenizableStringV1>(TokenizableStringV1::class.java) {
    override fun serialize(
        value: TokenizableStringV1,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        val className = when (value) {
            is TokenizableStringV1.Raw -> RAW
            is TokenizableStringV1.Tokenized -> TOKENIZED
        }
        gen.writeStartObject()
        gen.writeStringField(FIELD_CLASS_NAME, className)
        gen.writeStringField(FIELD_VALUE, value.value)
        gen.writeEndObject()
    }
}

/**
 * JSON deserializer for [TokenizableStringV1] that handles backward compatibility.
 *
 * Any json object without the explicit className specification will be resolved
 * as [TokenizableStringV1.Raw] for backward compatibility.
 *
 * Examples:
 * { "className": "TokenizableString.Raw", "value": "person" }
 *     -> TokenizableStringV1.Raw(value = "person")
 *
 * { "className": "TokenizableString.Tokenized", "value": "eq2Efc98d" }
 *     -> TokenizableStringV1.Tokenized(value = "eq2Efc98d")
 *
 * { "className": "Something else", "value": "name" }
 *     -> TokenizableStringV1.Raw(value = "name")
 *
 * { "value": "no class" }
 *     -> TokenizableStringV1.Raw(value = "no class")
 */
class TokenizableStringV1Deserializer : StdDeserializer<TokenizableStringV1>(TokenizableStringV1::class.java) {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): TokenizableStringV1 {
        val node: JsonNode = p.codec.readTree(p)

        val className = node[FIELD_CLASS_NAME]?.asText() ?: ""
        val value = node[FIELD_VALUE].asText()

        return when (className) {
            TOKENIZED -> TokenizableStringV1.Tokenized(value)
            else -> TokenizableStringV1.Raw(value)
        }
    }
}
