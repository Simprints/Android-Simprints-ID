package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.externalcredential.screens.scanocr.model.DetectedOcrBlock
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

internal class GetExternalCredentialBasedOnConfidenceUseCaseTest {
    private lateinit var useCase: GetExternalCredentialBasedOnConfidenceUseCase

    private val credentialLength3 = 3
    private val credentialLengthNhis = 8
    private val credentialLengthGhanaID = 15

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GetExternalCredentialBasedOnConfidenceUseCase()
    }

    private fun createBlock(value: String) = mockk<DetectedOcrBlock> {
        every { readoutValue } returns value
    }

    @Test
    fun `returns most frequent character at each position`() {
        val blocks = listOf(
            createBlock("ABC"),
            createBlock("ACD"),
            createBlock("CCD"),
        )

        val result = useCase(blocks, credentialLength3)

        assertThat(result).isEqualTo("ACD")
    }

    @Test
    fun `returns single value when only one block provided`() {
        val nhisMembership = "12345678"
        val blocks = listOf(createBlock(nhisMembership))

        val result = useCase(blocks, credentialLengthNhis)

        assertThat(result).isEqualTo(nhisMembership)
    }

    @Test
    fun `filters out blocks with different lengths`() {
        val blocks = listOf(
            createBlock("ABCDE"),
            createBlock("ACD"),
            createBlock("ACDGH"),
        )

        val result = useCase(blocks, credentialLength3)

        assertThat(result).isEqualTo("ACD")
    }

    @Test
    fun `handles identical strings correctly`() {
        val nhisMembership = "12345678"
        val blocks = listOf(
            createBlock(nhisMembership),
            createBlock(nhisMembership),
            createBlock(nhisMembership),
        )

        val result = useCase(blocks, credentialLengthNhis)

        assertThat(result).isEqualTo(nhisMembership)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception when block list is empty`() {
        val blocks = emptyList<DetectedOcrBlock>()

        useCase(blocks, credentialLength3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception when all blocks filtered out by length`() {
        val blocks = listOf(
            createBlock("AB"),
            createBlock("ABCD"),
            createBlock("ABCDE"),
        )

        useCase(blocks, credentialLength3)
    }

    @Test
    fun `handles single character strings`() {
        val blocks = listOf(
            createBlock("A"),
            createBlock("B"),
            createBlock("A"),
        )

        val result = useCase(blocks, 1)

        assertThat(result).isEqualTo("A")
    }

    @Test
    fun `constructs credential from multiple varying positions`() {
        val ghanaId = "GHA-123456789-0"
        val blocks = listOf(
            createBlock("GHA-123456789-0"),
            createBlock("GHA-123456789-1"),
            createBlock("GHA-123456789-0"),
        )

        val result = useCase(blocks, credentialLengthGhanaID)

        assertThat(result).isEqualTo(ghanaId)
    }

    @Test
    fun `uses only blocks matching credential length`() {
        val targetLength = 5
        val blocks = listOf(
            createBlock("ABCDE"),
            createBlock("FGHIJ"),
            createBlock("AB"),
            createBlock("ABCDEFGH"),
        )

        val result = useCase(blocks, targetLength)

        assertThat(result.length).isEqualTo(targetLength)
    }
}
