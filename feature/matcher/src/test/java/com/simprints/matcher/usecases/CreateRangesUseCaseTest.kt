package com.simprints.matcher.usecases

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CreateRangesUseCaseTest {

    private val useCase = CreateRangesUseCase()

    @Test
    fun `Returns empty list if no total`() {
        assertThat(useCase.invoke(0, 5)).isEqualTo(emptyList<IntRange>())
    }

    @Test
    fun `Returns single item if max withing single batch`() {
        assertThat(useCase.invoke(20, 25)).isEqualTo(listOf(0 until 20))
    }

    @Test
    fun `Correctly calculates last batch reminder`() {
        assertThat(useCase.invoke(17, 10)).isEqualTo(
            listOf(
                0 until 10,
                10 until 17,
            )
        )
    }

    @Test
    fun `Correctly calculates ranges for exact batches`() {
        assertThat(useCase.invoke(210, 10)).isEqualTo(
            listOf(
                0 until 10,    // size=10
                10 until 20,   // size=10
                20 until 40,   // size=20
                40 until 70,   // size=30
                70 until 110,  // size=40
                110 until 160, // size=50
                160 until 210, // size=50
            )
        )
    }
}
