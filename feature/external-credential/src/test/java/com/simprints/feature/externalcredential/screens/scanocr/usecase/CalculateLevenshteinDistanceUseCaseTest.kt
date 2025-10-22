package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test

internal class CalculateLevenshteinDistanceUseCaseTest {
    private lateinit var useCase: CalculateLevenshteinDistanceUseCase

    private val kitten = "kitten"
    private val sitting = "sitting"
    private val abc = "ABC"
    private val acd = "ACD"
    private val hello = "hello"
    private val emptyString = ""
    private val singleChar = "A"
    private val differentSingleChar = "B"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = CalculateLevenshteinDistanceUseCase()
    }

    private fun calculateDistance(
        s1: String,
        s2: String,
    ) = useCase(s1, s2)

    @Test
    fun `returns zero for identical strings`() {
        val result = calculateDistance(hello, hello)
        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `calculates correct distance for kitten to sitting example`() {
        val expected = 3
        val result = calculateDistance(kitten, sitting)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `calculates correct distance for ABC to ACD example`() {
        val expected = 2
        val result = calculateDistance(abc, acd)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `returns length when one string is empty`() {
        val result1 = calculateDistance(emptyString, hello)
        val result2 = calculateDistance(hello, emptyString)

        assertThat(result1).isEqualTo(hello.length)
        assertThat(result2).isEqualTo(hello.length)
    }

    @Test
    fun `returns zero for two empty strings`() {
        val result = calculateDistance(emptyString, emptyString)
        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `calculates distance for single character strings`() {
        val expectedSame = 0
        val expectedDifferent = 1

        val resultSame = calculateDistance(singleChar, singleChar)
        val resultDifferent = calculateDistance(singleChar, differentSingleChar)

        assertThat(resultSame).isEqualTo(expectedSame)
        assertThat(resultDifferent).isEqualTo(expectedDifferent)
    }

    @Test
    fun `handles strings with different lengths`() {
        val short = "cat"
        val long = "catching"
        val expected = 5

        val result = calculateDistance(short, long)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `calculates symmetric distance`() {
        val s1 = "algorithm"
        val s2 = "altruistic"

        val result1 = calculateDistance(s1, s2)
        val result2 = calculateDistance(s2, s1)

        assertThat(result1).isEqualTo(result2)
    }

    @Test
    fun `calculates distance for completely different strings of same length`() {
        val s1 = "ABCD"
        val s2 = "EFGH"
        val expected = 4

        val result = calculateDistance(s1, s2)
        assertThat(result).isEqualTo(expected)
    }
}
