package com.simprints.core.tools.extentions

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StringExtTest {
    @Test
    fun `Is valid UUID`() {
        assertThat("".isValidGuid()).isFalse()
        assertThat("test".isValidGuid()).isFalse()

        assertThat("63d26965-e68c-447c-9ee9-5aba2ebf589c".isValidGuid()).isTrue()
        assertThat("85500708-52D9-4A06-855F-391469E5C220".isValidGuid()).isTrue()
    }
}
