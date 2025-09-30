package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test

internal class GhanaIdCardOcrSelectorUseCaseTest {

    private lateinit var useCase: GhanaIdCardOcrSelectorUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GhanaIdCardOcrSelectorUseCase()
    }

    @Test
    fun `Returns true for valid Ghana ID formats`() {
        val validIds = listOf(
            "GHA-123456789-0",
            "GHA-987654321-5",
            "GHA-000000000-9"
        )

        validIds.forEach { id ->
            assertThat(useCase(id)).isTrue()
        }
    }

    @Test
    fun `Returns false for invalid Ghana ID formats`() {
        val invalidIds = listOf(
            "GHB-123456789-0",
            "GHA123456789-0",
            "GHA-1234567890",
            "GHA-12345678-0",
            "GHA-1234567890-0",
            "GHA-12345678A-0",
            "GHA-123456789-A",
            "GHA-123456789-01",
            "",
            "GHA-123456789-0 "
        )

        invalidIds.forEach { id ->
            assertThat(useCase(id)).isFalse()
        }
    }
}
