package com.simprints.core.domain.tokenization.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.json.JsonHelper
import org.junit.Ignore
import org.junit.Test

class TokenizationSerializerTest {
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

    @Ignore("Not implemented anymore to serialize the value only to plain text ")
    @Test
    fun `string tokenization serialization should produce plain string`() {
        val encrypted = "encrypted".asTokenizableEncrypted()
        val raw = "raw".asTokenizableRaw()

        val encryptedJson = JsonHelper.json.encodeToString(encrypted)
        val rawJson = JsonHelper.json.encodeToString(raw)

        assertThat(encryptedJson).isEqualTo("\"${encrypted.value}\"")
        assertThat(rawJson).isEqualTo("\"${raw.value}\"")
    }
}
