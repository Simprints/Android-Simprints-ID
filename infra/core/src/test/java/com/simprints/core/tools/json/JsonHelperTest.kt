package com.simprints.core.tools.json

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.testtools.common.syntax.assertThrows
import kotlinx.serialization.json.jsonObject
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

        val resultRaw = JsonHelper.json.encodeToString(tokenizableRaw)
        val resultEncrypted = JsonHelper.json.encodeToString(tokenizableEncrypted)

        val rawJson = JsonHelper.json.parseToJsonElement(resultRaw).jsonObject
        val encryptedJson = JsonHelper.json.parseToJsonElement(resultEncrypted).jsonObject

        assertThat(rawJson["className"].toString()).isEqualTo("\"TokenizableString.Raw\"")
        assertThat(rawJson["value"].toString()).isEqualTo("\"$tokenizableRaw\"")

        assertThat(encryptedJson["className"].toString()).isEqualTo("\"TokenizableString.Tokenized\"")
        assertThat(encryptedJson["value"].toString()).isEqualTo("\"$tokenizableEncrypted\"")
    }

    @Test
    fun `when module is passed, its serializer is used to read from JSON`() {
        val tokenizableRaw = "tokenizableRaw"
        val tokenizableEncrypted = "tokenizableEncrypted"
        val jsonRaw = "{\"className\":\"TokenizableString.Raw\",\"value\":\"${tokenizableRaw}\"}"
        val jsonTokenized = "{\"className\":\"TokenizableString.Tokenized\",\"value\":\"${tokenizableEncrypted}\"}"

        val resultRaw = JsonHelper.json.decodeFromString<TokenizableString>(jsonRaw)
        val resultEncrypted = JsonHelper.json.decodeFromString<TokenizableString>(jsonTokenized)
        assertThat(resultRaw).isEqualTo(tokenizableRaw.asTokenizableRaw())
        assertThat(resultEncrypted).isEqualTo(tokenizableEncrypted.asTokenizableEncrypted())
    }
}
