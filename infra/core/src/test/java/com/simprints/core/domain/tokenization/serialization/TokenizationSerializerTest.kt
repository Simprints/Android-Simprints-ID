package com.simprints.core.domain.tokenization.serialization

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.serialization.SimJson
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class TokenizationSerializerTest {
    @Test
    fun `serialize Raw produces className Raw and value`() {
        val raw = TokenizableString.Raw("person")

        val result = SimJson.parseToJsonElement(
            SimJson.encodeToString(raw),
        )

        val expected = JsonObject(
            mapOf(
                "className" to JsonPrimitive("TokenizableString.Raw"),
                "value" to JsonPrimitive("person"),
            ),
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `serialize Tokenized produces className Tokenized and value`() {
        val tokenized = TokenizableString.Tokenized("eq2Efc98d")

        val result = SimJson.parseToJsonElement(
            SimJson.encodeToString(tokenized),
        )

        val expected = JsonObject(
            mapOf(
                "className" to JsonPrimitive("TokenizableString.Tokenized"),
                "value" to JsonPrimitive("eq2Efc98d"),
            ),
        )

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `deserialize Raw json produces Raw instance`() {
        val jsonInput =
            """{"className":"TokenizableString.Raw","value":"person"}"""

        val result = SimJson.decodeFromString<TokenizableString>(jsonInput)

        assertThat(result).isEqualTo(TokenizableString.Raw("person"))
    }

    @Test
    fun `deserialize Tokenized json produces Tokenized instance`() {
        val jsonInput =
            """{"className":"TokenizableString.Tokenized","value":"eq2Efc98d"}"""

        val result = SimJson.decodeFromString<TokenizableString>(jsonInput)

        assertThat(result).isEqualTo(TokenizableString.Tokenized("eq2Efc98d"))
    }

    @Test
    fun `deserialize unknown className defaults to Raw`() {
        val jsonInput =
            """{"className":"SomethingElse","value":"name"}"""

        val result = SimJson.decodeFromString<TokenizableString>(jsonInput)

        assertThat(result).isEqualTo(TokenizableString.Raw("name"))
    }

    @Test
    fun `deserialize missing className defaults to Raw`() {
        val jsonInput =
            """{"value":"no class"}"""

        val result = SimJson.decodeFromString<TokenizableString>(jsonInput)

        assertThat(result).isEqualTo(TokenizableString.Raw("no class"))
    }

    @Test(expected = IllegalStateException::class)
    fun `deserialize missing value throws`() {
        val jsonInput =
            """{"className":"TokenizableString.Raw"}"""

        SimJson.decodeFromString<TokenizableString>(jsonInput)
    }

    @Test
    fun `round trip Raw preserves equality`() {
        val original = TokenizableString.Raw("raw")

        val jsonValue = SimJson.encodeToString(original)
        val decoded = SimJson.decodeFromString<TokenizableString>(jsonValue)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `round trip Tokenized preserves equality`() {
        val original = TokenizableString.Tokenized("encrypted")

        val jsonValue = SimJson.encodeToString(original)
        val decoded = SimJson.decodeFromString<TokenizableString>(jsonValue)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `deserialize plain string defaults to Raw`() {
        // Verifies that a plain JSON string (legacy format) deserializes correctly into
        // a TokenizableString.Raw instance, ensuring backward compatibility with older payloads
        // where TokenizableString was represented simply as a string instead of an object.

        val jsonInput = """"legacy-string""""

        val result = SimJson.decodeFromString<TokenizableString>(jsonInput)

        assertThat(result).isEqualTo(
            TokenizableString.Raw("legacy-string"),
        )
    }
}
