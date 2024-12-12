package com.simprints.core.tools.json

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameDeserializer
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameSerializer
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Test

class JsonHelperTest {
    @Test
    fun validateJson() {
        val json = """{"name": "Test" }"""
        JsonHelper.validateJsonOrThrow(json)
    }

    @Test
    fun validateJson_shouldThrownIfInvalid() {
        val json = """{"name": "Test }"""
        assertThrows<Throwable> {
            JsonHelper.validateJsonOrThrow(json)
        }
    }

    @Test
    fun `when module is passed, its serializer is used to parse to JSON`() {
        val tokenizableRaw = "tokenizableRaw".asTokenizableRaw()
        val tokenizableEncrypted = "tokenizableEncrypted".asTokenizableEncrypted()

        val resultRaw = JsonHelper.toJson(tokenizableRaw, tokenizableStringModule)
        val resultEncrypted = JsonHelper.toJson(tokenizableEncrypted, tokenizableStringModule)

        assertThat(resultRaw).isEqualTo("{\"className\":\"TokenizableString.Raw\",\"value\":\"${tokenizableRaw}\"}")
        assertThat(resultEncrypted).isEqualTo("{\"className\":\"TokenizableString.Tokenized\",\"value\":\"${tokenizableEncrypted}\"}")
    }

    @Test
    fun `when module is passed, its serializer is used to read from JSON`() {
        val tokenizableRaw = "tokenizableRaw"
        val tokenizableEncrypted = "tokenizableEncrypted"
        val jsonRaw = "{\"className\":\"TokenizableString.Raw\",\"value\":\"${tokenizableRaw}\"}"
        val jsonTokenized = "{\"className\":\"TokenizableString.Tokenized\",\"value\":\"${tokenizableEncrypted}\"}"

        val resultRaw = JsonHelper.fromJson(
            json = jsonRaw,
            module = tokenizableStringModule,
            type = object : TypeReference<TokenizableString>() {},
        )
        val resultEncrypted = JsonHelper.fromJson(
            json = jsonTokenized,
            module = tokenizableStringModule,
            type = object : TypeReference<TokenizableString>() {},
        )

        assertThat(resultRaw).isEqualTo(tokenizableRaw.asTokenizableRaw())
        assertThat(resultEncrypted).isEqualTo(tokenizableEncrypted.asTokenizableEncrypted())
    }

    companion object {
        val tokenizableStringModule = SimpleModule().apply {
            addSerializer(TokenizableString::class.java, TokenizationClassNameSerializer())
            addDeserializer(TokenizableString::class.java, TokenizationClassNameDeserializer())
        }
    }
}
