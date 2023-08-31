package com.simprints.core.tools.extentions

import com.google.common.truth.Truth.assertThat
import org.junit.Test


class AnyExtTest {

    @Test
    fun `given no exception is caught, when tryOrNull is called, then value is returned`() {
        val expected = ""
        val result = tryOrNull { expected }
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `given exception is thrown, when tryOrNull is called, then null is returned`() {
        val result: String? = tryOrNull<String> { throw Exception() }
        assertThat(result).isNull()
    }
}