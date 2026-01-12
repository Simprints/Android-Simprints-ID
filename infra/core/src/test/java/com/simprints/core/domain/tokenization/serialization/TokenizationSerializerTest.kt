package com.simprints.core.domain.tokenization.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.json.JsonHelper.json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Ignore
import org.junit.Test

class TokenizationSerializerTest {
    // Todo remove this test once removing all the jackson code
    @Test
    fun `class name tokenization serialization and deserialization should produce same result`() {
        val encrypted = "encrypted".asTokenizableEncrypted()
        val raw = "raw".asTokenizableRaw()

        val module = SimpleModule().apply {
            addSerializer(TokenizableString::class.java, TokenizationClassNameSerializer())
            addDeserializer(TokenizableString::class.java, TokenizationClassNameDeserializer())
        }
        val jackson: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(module)

        val encryptedJson = jackson.writeValueAsString(encrypted)
        val rawJson = jackson.writeValueAsString(raw)

        val encryptedFromJson = jackson.readValue(encryptedJson, TokenizableString::class.java)
        val rawFromJson = jackson.readValue(rawJson, TokenizableString::class.java)

        assertThat(encryptedFromJson).isEqualTo(encrypted)
        assertThat(rawFromJson).isEqualTo(raw)
    }

    @Test
    fun `serialize Raw produces className Raw and value`() {
        val raw = TokenizableString.Raw("person")

        val result = json.parseToJsonElement(
            json.encodeToString(raw),
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

        val result = json.parseToJsonElement(
            json.encodeToString(tokenized),
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

        val result = json.decodeFromString<TokenizableString>(jsonInput)

        assertThat(result).isEqualTo(TokenizableString.Raw("person"))
    }

    @Test
    fun `deserialize Tokenized json produces Tokenized instance`() {
        val jsonInput =
            """{"className":"TokenizableString.Tokenized","value":"eq2Efc98d"}"""

        val result = json.decodeFromString<TokenizableString>(jsonInput)

        assertThat(result).isEqualTo(TokenizableString.Tokenized("eq2Efc98d"))
    }

    @Test
    fun `deserialize unknown className defaults to Raw`() {
        val jsonInput =
            """{"className":"SomethingElse","value":"name"}"""

        val result = json.decodeFromString<TokenizableString>(jsonInput)

        assertThat(result).isEqualTo(TokenizableString.Raw("name"))
    }

    @Test
    fun `deserialize missing className defaults to Raw`() {
        val jsonInput =
            """{"value":"no class"}"""

        val result = json.decodeFromString<TokenizableString>(jsonInput)

        assertThat(result).isEqualTo(TokenizableString.Raw("no class"))
    }

    @Test(expected = IllegalStateException::class)
    fun `deserialize missing value throws`() {
        val jsonInput =
            """{"className":"TokenizableString.Raw"}"""

        json.decodeFromString<TokenizableString>(jsonInput)
    }

    @Test
    fun `round trip Raw preserves equality`() {
        val original = TokenizableString.Raw("raw")

        val jsonValue = json.encodeToString(original)
        val decoded = json.decodeFromString<TokenizableString>(jsonValue)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `round trip Tokenized preserves equality`() {
        val original = TokenizableString.Tokenized("encrypted")

        val jsonValue = json.encodeToString(original)
        val decoded = json.decodeFromString<TokenizableString>(jsonValue)

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `deserialize plain string defaults to Raw`() {
        // Verifies that a plain JSON string (legacy format) deserializes correctly into
        // a TokenizableString.Raw instance, ensuring backward compatibility with older payloads
        // where TokenizableString was represented simply as a string instead of an object.

        val jsonInput = """"legacy-string""""

        val result = json.decodeFromString<TokenizableString>(jsonInput)

        assertThat(result).isEqualTo(
            TokenizableString.Raw("legacy-string"),
        )
    }
}
