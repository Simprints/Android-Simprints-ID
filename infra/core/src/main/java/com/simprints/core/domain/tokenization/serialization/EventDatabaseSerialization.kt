package com.simprints.core.domain.tokenization.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.simprints.core.domain.tokenization.TokenizableString

private const val TOKENIZED = "TokenizableString.Tokenized"
const val RAW = "TokenizableString.Raw"
const val FIELD_CLASS_NAME = "className"
const val FIELD_VALUE = "value"

/**
 * JSON serializer for the [TokenizableString] that creates an explicit specification of the child
 * class being used to create a JSON.
 * Use [TokenizationClassNameDeserializer] to deserialize the object.
 *
 * Examples:
 * TokenizableString.Raw(value = "person")
 *     -> { "className": "TokenizableString.Raw", "value": "person" }
 *
 * TokenizableString.Tokenized(value = "eq2Efc98d")
 *     -> { "className": "TokenizableString.Tokenized", "value": "eq2Efc98d" }
 */
class TokenizationClassNameSerializer : StdSerializer<TokenizableString>(TokenizableString::class.java) {
    override fun serialize(
        value: TokenizableString,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        val className = when (value) {
            is TokenizableString.Raw -> RAW
            is TokenizableString.Tokenized -> TOKENIZED
        }
        gen.writeStartObject()
        gen.writeStringField(FIELD_CLASS_NAME, className)
        gen.writeStringField(FIELD_VALUE, value.value)
        gen.writeEndObject()
    }
}

/**
 * JSON deserializer for the [TokenizableString] that takes the JSON node with an explicit
 * specification of the child class. Any json object without the explicit className specification
 * will be resolved as [TokenizableString.Raw].
 * Use [TokenizationClassNameSerializer] to deserialize the object.
 *
 * Examples:
 * { "className": "TokenizableString.Raw", "value": "person" }
 *     -> TokenizableString.Raw(value = "person")
 *
 * { "className": "TokenizableString.Tokenized", "value": "eq2Efc98d" }
 *     -> TokenizableString.Tokenized(value = "eq2Efc98d")
 *
 * { "className": "Something else", "value": "name" }
 *     -> TokenizableString.Raw(value = "name")
 *
 * { "value": "no class" }
 *     -> TokenizableString.Raw(value = "no class")
 */
class TokenizationClassNameDeserializer : StdDeserializer<TokenizableString>(TokenizableString::class.java) {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): TokenizableString {
        val node: JsonNode = p.codec.readTree(p)

        val className = node[FIELD_CLASS_NAME]?.asText() ?: ""
        val value = node[FIELD_VALUE].asText()

        return when (className) {
            TOKENIZED -> TokenizableString.Tokenized(value)
            else -> TokenizableString.Raw(value)
        }
    }
}
