package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

internal class FindBestTextBlockForCredentialUseCaseTest {

    @MockK
    private lateinit var calculateLevenshteinDistanceUseCase: CalculateLevenshteinDistanceUseCase
    @MockK
    private lateinit var useCase: FindBestTextBlockForCredentialUseCase

    private val targetCredential = "GHA-123456789-0"
    private val exactMatch = "GHA-123456789-0"
    private val closeMatch = "GHA-123456789-1"
    private val distantMatch = "ABC-987654321-9"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = FindBestTextBlockForCredentialUseCase(calculateLevenshteinDistanceUseCase)
    }

    private fun createBlock(readoutValue: String) = mockk<DetectedOcrBlock> {
        every { this@mockk.readoutValue } returns readoutValue
        every { copy(readoutValue = any()) } returns this@mockk
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception when block list is empty`() {
        val emptyBlocks = emptyList<DetectedOcrBlock>()

        useCase(targetCredential, emptyBlocks)
    }

    @Test
    fun `returns exact match when found`() {
        val exactMatchBlock = createBlock(exactMatch)
        val otherBlock = createBlock(closeMatch)
        val blocks = listOf(otherBlock, exactMatchBlock)

        val result = useCase(targetCredential, blocks)

        assertThat(result).isEqualTo(exactMatchBlock)
        verify(exactly = 0) { calculateLevenshteinDistanceUseCase(any(), any()) }
    }

    @Test
    fun `returns last exact match when multiple found`() {
        val firstExactMatch = createBlock(exactMatch)
        val secondExactMatch = createBlock(exactMatch)
        val blocks = listOf(firstExactMatch, secondExactMatch)

        val result = useCase(targetCredential, blocks)

        assertThat(result).isEqualTo(secondExactMatch)
    }

    @Test
    fun `searches blocks in reverse order for exact match`() {
        val firstBlock = createBlock(closeMatch)
        val lastBlock = createBlock(exactMatch)
        val blocks = listOf(firstBlock, lastBlock)

        val result = useCase(targetCredential, blocks)

        assertThat(result).isEqualTo(lastBlock)
    }

    @Test
    fun `uses levenshtein distance when no exact match found`() {
        val closeBlock = createBlock(closeMatch)
        val distantBlock = createBlock(distantMatch)
        val blocks = listOf(distantBlock, closeBlock)

        every { calculateLevenshteinDistanceUseCase(targetCredential, closeMatch) } returns 1
        every { calculateLevenshteinDistanceUseCase(targetCredential, distantMatch) } returns 10

        useCase(targetCredential, blocks)

        verify { calculateLevenshteinDistanceUseCase.invoke(targetCredential, closeMatch) }
        verify { calculateLevenshteinDistanceUseCase.invoke(targetCredential, distantMatch) }
        verify { closeBlock.copy(readoutValue = targetCredential) }
    }

    @Test
    fun `returns block with smallest levenshtein distance`() {
        val closeBlock = createBlock(closeMatch)
        val distantBlock = createBlock(distantMatch)
        val blocks = listOf(closeBlock, distantBlock)

        every { calculateLevenshteinDistanceUseCase(targetCredential, closeMatch) } returns 1
        every { calculateLevenshteinDistanceUseCase(targetCredential, distantMatch) } returns 10

        useCase(targetCredential, blocks)

        verify { closeBlock.copy(readoutValue = targetCredential) }
    }

    @Test
    fun `updates credential value when using levenshtein distance`() {
        val block = createBlock(closeMatch)
        val blocks = listOf(block)
        val updatedBlock = mockk<DetectedOcrBlock>()

        every { calculateLevenshteinDistanceUseCase(targetCredential, closeMatch) } returns 1
        every { block.copy(readoutValue = targetCredential) } returns updatedBlock

        val result = useCase(targetCredential, blocks)

        assertThat(result).isEqualTo(updatedBlock)
        verify { block.copy(readoutValue = targetCredential) }
    }

    @Test
    fun `handles single block with exact match`() {
        val block = createBlock(exactMatch)
        val blocks = listOf(block)

        val result = useCase(targetCredential, blocks)

        assertThat(result).isEqualTo(block)
        verify(exactly = 0) { calculateLevenshteinDistanceUseCase.invoke(any(), any()) }
    }

    @Test
    fun `handles equal levenshtein distances by returning first found`() {
        val firstBlock = createBlock(closeMatch)
        val secondBlock = createBlock(distantMatch)
        val blocks = listOf(firstBlock, secondBlock)

        every { calculateLevenshteinDistanceUseCase(targetCredential, closeMatch) } returns 1
        every { calculateLevenshteinDistanceUseCase(targetCredential, distantMatch) } returns 1

        useCase(targetCredential, blocks)

        verify { firstBlock.copy(readoutValue = targetCredential) }
    }
}
