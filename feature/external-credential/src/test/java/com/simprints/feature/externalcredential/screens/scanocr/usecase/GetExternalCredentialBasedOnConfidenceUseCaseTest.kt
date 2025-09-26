package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import com.simprints.feature.externalcredential.screens.scanocr.model.OcrDocumentType
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

internal class GetExternalCredentialBasedOnConfidenceUseCaseTest {

    private lateinit var useCase: GetExternalCredentialBasedOnConfidenceUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GetExternalCredentialBasedOnConfidenceUseCase()
    }

    @Test
    fun `returns most frequent character at each position`() {
        val blocks = listOf(
            mockk<DetectedOcrBlock> { every { readoutValue } returns "ABC" },
            mockk<DetectedOcrBlock> { every { readoutValue } returns "ACD" },
            mockk<DetectedOcrBlock> { every { readoutValue } returns "CCD" }
        )

        val result = useCase(blocks)

        assertThat(result).isEqualTo("ACD")
    }

    @Test
    fun `returns single value when only one block provided`() {
        val ghanaId = "GHA-123456789-0"
        val blocks = listOf(
            mockk<DetectedOcrBlock> { every { readoutValue } returns ghanaId }
        )

        val result = useCase(blocks)

        assertThat(result).isEqualTo(ghanaId)
    }

    @Test
    fun `truncates to shortest length when blocks have different lengths`() {
        val blocks = listOf(
            mockk<DetectedOcrBlock> {
                every { readoutValue } returns "ABCDE"
                every { documentType } returns OcrDocumentType.GhanaIdCard
            },
            mockk<DetectedOcrBlock> { every { readoutValue } returns "ACD" },
            mockk<DetectedOcrBlock> { every { readoutValue } returns "ACDGH" }
        )

        val result = useCase(blocks)

        assertThat(result).isEqualTo("ACD")
    }

    @Test
    fun `handles identical strings correctly`() {
        val nhisMembership = "12345678"
        val blocks = listOf(
            mockk<DetectedOcrBlock> { every { readoutValue } returns nhisMembership },
            mockk<DetectedOcrBlock> { every { readoutValue } returns nhisMembership },
            mockk<DetectedOcrBlock> { every { readoutValue } returns nhisMembership }
        )

        val result = useCase(blocks)

        assertThat(result).isEqualTo(nhisMembership)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception when block list is empty`() {
        val blocks = emptyList<DetectedOcrBlock>()

        useCase(blocks)
    }

    @Test
    fun `handles single character strings`() {
        val blocks = listOf(
            mockk<DetectedOcrBlock> { every { readoutValue } returns "A" },
            mockk<DetectedOcrBlock> { every { readoutValue } returns "B" },
            mockk<DetectedOcrBlock> { every { readoutValue } returns "A" }
        )

        val result = useCase(blocks)

        assertThat(result).isEqualTo("A")
    }
}
