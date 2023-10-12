package com.simprints.core.domain.tokenization

import com.google.common.truth.Truth.assertThat
import org.junit.Test


class TokenizableStringTest {

    @Test
    fun `given two different TokenizedString objects with equal values, when compared, should be equal`() {
        val value = "value"
        assertThat(TokenizableString.Raw(value).equals(TokenizableString.Tokenized(value))).isTrue()
    }

    @Test
    fun `given two similar TokenizedString objects with different values, when compared, should be equal`() {
        val value = "value"
        val value2 = "value2"
        assertThat(TokenizableString.Raw(value) == TokenizableString.Raw(value2)).isFalse()
        assertThat(TokenizableString.Tokenized(value) == TokenizableString.Tokenized(value2)).isFalse()
    }

    @Test
    fun `asTokenizableRaw should return TokenizedString_Raw`() {
        val value = "value"
        assertThat(value.asTokenizableRaw()).isEqualTo(TokenizableString.Raw(value))
    }

    @Test
    fun `asTokenizableEncrypted should return TokenizedString_Tokenized`() {
        val value = "value"
        assertThat(value.asTokenizableEncrypted()).isEqualTo(TokenizableString.Tokenized(value))
    }
    @Test
    fun `isTokenized should return true when string is TokenizedString_Tokenized`() {
        val value = "value".asTokenizableEncrypted()
        assertThat(value.isTokenized()).isTrue()
    }
    @Test
    fun `isTokenized should return false when string is TokenizedString_Raw`() {
        val value = "value".asTokenizableRaw()
        assertThat(value.isTokenized()).isFalse()
    }
}