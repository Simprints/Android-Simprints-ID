package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test

internal class GhanaNhisCardOcrSelectorUseCaseTest {

    private lateinit var useCase: GhanaNhisCardOcrSelectorUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GhanaNhisCardOcrSelectorUseCase()
    }

    @Test
    fun `Returns true for valid NHIS membership numbers`() {
        val validNumbers = listOf(
            "12345678",
            "98765432",
            "00000000"
        )

        validNumbers.forEach { number ->
            assertThat(useCase(number)).isTrue()
        }
    }

    @Test
    fun `Returns false for invalid NHIS membership numbers`() {
        val invalidNumbers = listOf(
            "1234567",
            "123456789",
            "1234567A",
            "12345-78",
            "",
        )

        invalidNumbers.forEach { number ->
            assertThat(useCase(number)).isFalse()
        }
    }
}
