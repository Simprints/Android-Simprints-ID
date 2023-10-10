package com.simprints.core.domain.tokenization

import com.google.common.truth.Truth.assertThat
import org.junit.Test


class TokenizableStringTest {

    @Test
    fun `given two different TokenizableString objects with equal values, when compared, should be equal`() {
        val value = "value"
        assertThat(TokenizableString.Raw(value).equals(TokenizableString.Tokenized(value))).isTrue()
    }

    @Test
    fun `given two similar TokenizableString objects with different values, when compared, should be equal`() {
        val value = "value"
        val value2 = "value2"
        assertThat(TokenizableString.Raw(value) == TokenizableString.Raw(value2)).isFalse()
        assertThat(TokenizableString.Tokenized(value) == TokenizableString.Tokenized(value2)).isFalse()
    }

    @Test
    fun `asTokenizedRaw should return TokenizableString_Raw`() {
        val value = "value"
        assertThat(value.asTokenizedRaw()).isEqualTo(TokenizableString.Raw(value))
    }

    @Test
    fun `asTokenizedEncrypted should return TokenizableString_Tokenized`() {
        val value = "value"
        assertThat(value.asTokenizedEncrypted()).isEqualTo(TokenizableString.Tokenized(value))
    }
    @Test
    fun `isTokenized should return true when string is TokenizableString_Tokenized`() {
        val value = "value".asTokenizedEncrypted()
        assertThat(value.isTokenized()).isTrue()
    }
    @Test
    fun `isTokenized should return false when string is TokenizableString_Raw`() {
        val value = "value".asTokenizedRaw()
        assertThat(value.isTokenized()).isFalse()
    }
}