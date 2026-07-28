package com.simprints.core.tools.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StringUtilsTest {
    @Test
    fun `Is valid UUID`() {
        assertThat("".isValidGuid()).isFalse()
        assertThat("test".isValidGuid()).isFalse()

        assertThat("63d26965-e68c-447c-9ee9-5aba2ebf589c".isValidGuid()).isTrue()
        assertThat("85500708-52D9-4A06-855F-391469E5C220".isValidGuid()).isTrue()
    }

    @Test
    fun `Is not valid UUID when malformed`() {
        assertThat("1-1-1-1-1".isValidGuid()).isFalse()
        assertThat("63d26965-e68c-447c-9ee9-5aba2ebf589".isValidGuid()).isFalse()
        assertThat(" 63d26965-e68c-447c-9ee9-5aba2ebf589c ".isValidGuid()).isFalse()
        assertThat("63d26965e68c447c9ee95aba2ebf589c".isValidGuid()).isFalse()
    }

    @Test
    fun `Is not valid UUID when version or variant is not supported`() {
        // Only versions 3 and 4 with RFC 4122 variants are accepted
        assertThat("0003eaf0-9044-6ead-fd6e-600d1b616ba5".isValidGuid()).isFalse()
        assertThat("123e4567-e89b-12d3-a456-426614174000".isValidGuid()).isFalse()
        assertThat("b2f3e4c5-6789-4fab-cdef-2345678901bc".isValidGuid()).isFalse()
    }

    @Test
    fun `randomUUID generates valid UUID`() {
        assertThat(randomUUID().isValidGuid()).isTrue()
    }
}
