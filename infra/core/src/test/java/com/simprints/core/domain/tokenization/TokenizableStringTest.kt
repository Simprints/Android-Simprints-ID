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

    @Test
    fun `when hashCode is invoked, the result is calculated from the value`() {
        val value = "value"
        assertThat(value.asTokenizableRaw().hashCode()).isEqualTo(value.hashCode())
        assertThat(value.asTokenizableEncrypted().hashCode()).isEqualTo(value.hashCode())
    }

    @Test
    fun `when toString is invoked, the result is calculated from the value`() {
        val value = "value"
        assertThat(value.asTokenizableRaw().toString()).isEqualTo(value)
        assertThat(value.asTokenizableEncrypted().toString()).isEqualTo(value)
    }

    @Test
    fun `when asTokenizable is invoked, the correct tokenizable string is returned`() {
        val value = "value"
        assertThat(value.asTokenizable(isTokenized = true)).isEqualTo(TokenizableString.Tokenized(value))
        assertThat(value.asTokenizable(isTokenized = false)).isEqualTo(TokenizableString.Raw(value))
    }

    @Test
    fun `when takeIfTokenized is invoked, the value is not null only if it is tokenized`() {
        val value = "value"
        assertThat(value.asTokenizableRaw().takeIfTokenized(value)).isNull()
        assertThat(value.asTokenizableEncrypted().takeIfTokenized(value)).isEqualTo(value)
    }
}
