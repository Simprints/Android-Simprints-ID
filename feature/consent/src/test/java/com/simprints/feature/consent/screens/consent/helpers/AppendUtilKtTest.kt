package com.simprints.feature.consent.screens.consent.helpers

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppendUtilKtTest {
    @Test
    fun `should append first sentence without leading space`() {
        val initialTextStringBuilder = StringBuilder()
        val actualText = initialTextStringBuilder.appendSentence("Sentence 1.").toString()
        val expectedText = "Sentence 1."

        assertThat(actualText).isEqualTo(expectedText)
    }

    @Test
    fun `should append non-first sentence with leading space`() {
        val initialTextStringBuilder = StringBuilder("Sentence 1.")
        val actualText = initialTextStringBuilder.appendSentence("Sentence 2.").toString()
        val expectedText = "Sentence 1. Sentence 2."

        assertThat(actualText).isEqualTo(expectedText)
    }
}
