package com.simprints.feature.exitform

import com.google.common.truth.Truth
import org.junit.Test

class ExitFormResultTest {
    @Test
    fun `exit form result is safe when option present`() {
        Truth.assertThat(ExitFormResult(true, null).submittedOption()).isNull()
        Truth.assertThat(ExitFormResult(false, null).submittedOption()).isNull()
    }

    @Test
    fun `exit form result is safe only when form submitted`() {
        Truth.assertThat(ExitFormResult(false, ExitFormOption.Other).submittedOption()).isNull()
        Truth.assertThat(ExitFormResult(true, ExitFormOption.Other).submittedOption()).isNotNull()
    }
}
