package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test

internal class GetExternalCredentialBasedOnConfidenceUseCaseTest {
    private lateinit var useCase: GetBestReadoutBasedOnConfidenceUseCase

    private val credentialLength3 = 3
    private val credentialLengthNhis = 8
    private val credentialLengthGhanaID = 15

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = GetBestReadoutBasedOnConfidenceUseCase()
    }

    @Test
    fun `returns most frequent character at each position`() {
        val blocks = listOf(
            "ABC",
            "ACD",
            "CCD",
        )

        val result = useCase(blocks, credentialLength3)

        assertThat(result).isEqualTo("ACD")
    }

    @Test
    fun `returns single value when only one block provided`() {
        val nhisMembership = "12345678"
        val blocks = listOf(nhisMembership)

        val result = useCase(blocks, credentialLengthNhis)

        assertThat(result).isEqualTo(nhisMembership)
    }

    @Test
    fun `filters out blocks with different lengths`() {
        val blocks = listOf(
            "ABCDE",
            "ACD",
            "ACDGH",
        )

        val result = useCase(blocks, credentialLength3)

        assertThat(result).isEqualTo("ACD")
    }

    @Test
    fun `handles identical strings correctly`() {
        val nhisMembership = "12345678"
        val blocks = listOf(
            nhisMembership,
            nhisMembership,
            nhisMembership,
        )

        val result = useCase(blocks, credentialLengthNhis)

        assertThat(result).isEqualTo(nhisMembership)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception when block list is empty`() {
        val blocks = emptyList<String>()

        useCase(blocks, credentialLength3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception when all blocks filtered out by length`() {
        val blocks = listOf(
            "AB",
            "ABCD",
            "ABCDE",
        )

        useCase(blocks, credentialLength3)
    }

    @Test
    fun `handles single character strings`() {
        val blocks = listOf(
            "A",
            "B",
            "A",
        )

        val result = useCase(blocks, 1)

        assertThat(result).isEqualTo("A")
    }

    @Test
    fun `constructs credential from multiple varying positions`() {
        val ghanaId = "GHA-123456789-0"
        val blocks = listOf(
            "GHA-123456789-0",
            "GHA-123456789-1",
            "GHA-123456789-0",
        )

        val result = useCase(blocks, credentialLengthGhanaID)

        assertThat(result).isEqualTo(ghanaId)
    }

    @Test
    fun `uses only blocks matching credential length`() {
        val targetLength = 5
        val blocks = listOf(
            "ABCDE",
            "FGHIJ",
            "AB",
            "ABCDEFGH",
        )

        val result = useCase(blocks, targetLength)

        assertThat(result.length).isEqualTo(targetLength)
    }
}
