package com.simprints.core.domain.tokenization.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.simprints.core.domain.tokenization.TokenizableString

/**
 * JSON serializer for the [TokenizableString] that writes it as a plain [String] object using its
 * [TokenizableString.value]
 *
 * Examples:
 * TokenizableString.Raw(value = "person")
 *     -> "person"
 *
 * TokenizableString.Tokenized(value = "eq2Efc98d")
 *     -> "eq2Efc98d"
 *
 * Wrapper(val someValue = TokenizableString.Tokenized(value = "eq2Efc98d"))
 *     -> { "someValue": "eq2Efc98d" }
 */
class TokenizationAsStringSerializer : StdSerializer<TokenizableString>(TokenizableString::class.java) {
    override fun serialize(
        value: TokenizableString,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeString(value.value)
    }
}
