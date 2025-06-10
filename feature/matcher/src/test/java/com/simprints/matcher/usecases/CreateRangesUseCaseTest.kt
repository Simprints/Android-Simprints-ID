package com.simprints.matcher.usecases

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CreateRangesUseCaseTest {

    @Test
    fun `should create correct ranges when numCandidates equals numConsumers`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 5)

        // When
        val result = useCase(5)

        // Then
        assertThat(result).containsExactly(
             0 until 1,
             1 until 2,
             2 until 3,
             3 until 4,
             4 until 5
        ).inOrder()
    }

    @Test
    fun `should create correct ranges when numCandidates is greater than numConsumers`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 3)

        // When
        val result = useCase(10)

        // Then
        assertThat(result).containsExactly(
            0 until 4,
            4 until 7,
            7 until 10
        ).inOrder()
    }

    @Test
    fun `should handle single item`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 4)

        // When
        val result = useCase(1)

        // Then
        assertThat(result).containsExactly(0 until 1).inOrder()
    }

    @Test
    fun `should handle totalCount equal to MAX_BATCH_SIZE`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 1)

        // When
        val result = useCase(2000)

        // Then
        assertThat(result).containsExactly(0 until 2000)
    }

    @Test
    fun `should handle batch sizes that are exactly MAX_BATCH_SIZE`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 2)

        // When
        val result = useCase(4000)

        // Then
        assertThat(result).containsExactly(
            0 until 2000,
            2000 until 4000
        ).inOrder()
    }

    @Test
    fun `should create correct ranges when numCandidates is less than numConsumers`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 5)

        // When
        val result = useCase(3)

        // Then
        assertThat(result).containsExactly(
            0 until 1,
            1 until 2,
            2 until 3
        ).inOrder()
    }

    @Test
    fun `should create correct ranges with uneven distribution`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 3)

        // When
        val result = useCase(11)

        // Then
        assertThat(result).containsExactly(
            0 until 4,
            4 until 8,
            8 until 11
        ).inOrder()
    }

    @Test
    fun `should create empty list when numCandidates is zero`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 5)

        // When
        val result = useCase(0)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should create single range when numConsumers is one`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 1)

        // When
        val result = useCase(10)

        // Then
        assertThat(result).containsExactly(0 until 10)
    }

    @Test
    fun `should handle large numbers correctly`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 4)

        // When
        val result = useCase(1000)

        // Then
        assertThat(result).containsExactly(
            0 until 250,
            250 until 500,
            500 until 750,
            750 until 1000
        ).inOrder()
    }

    @Test
    fun `should handle 2500 candidates with 4 processors`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 4)

        // When
        val result = useCase(2500)

        // Then
        // 4 processors, batches under 2000 each, so 4 total batches
        // Base size = 2500/4 = 625, remainder = 0
        assertThat(result).containsExactly(
            0 until 625,
            625 until 1250,
            1250 until 1875,
            1875 until 2500
        ).inOrder()
    }

    @Test
    fun `should handle 5000 candidates with 4 processors`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 4)

        // When
        val result = useCase(5000)

        // Then
        // 4 processors, base size approaching MAX_BATCH_SIZE, so still 4 batches
        // Base size = 5000/4 = 1250, remainder = 0
        assertThat(result).containsExactly(
            0 until 1250,
            1250 until 2500,
            2500 until 3750,
            3750 until 5000
        ).inOrder()
    }

    @Test
    fun `should handle 10000 candidates with 8 processors`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 8)

        // When
        val result = useCase(10000)

        // Then
        // 8 processors, base size = 10000/8 = 1250, remainder = 0
        assertThat(result).containsExactly(
            0 until 1250,
            1250 until 2500,
            2500 until 3750,
            3750 until 5000,
            5000 until 6250,
            6250 until 7500,
            7500 until 8750,
            8750 until 10000
        ).inOrder()
    }

    @Test
    fun `should limit batch size to 2000 for 15000 candidates with 4 processors`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 4)

        // When
        val result = useCase(15000)

        // Then
        // Each processor would get 15000/4 = 3750 items, exceeding MAX_BATCH_SIZE
        // Need ceiling(15000/(4*2000)) = 2 batches per processor = 8 total batches
        // Base size = 15000/8 = 1875, remainder = 0
        assertThat(result).containsExactly(
            0 until 1875,
            1875 until 3750,
            3750 until 5625,
            5625 until 7500,
            7500 until 9375,
            9375 until 11250,
            11250 until 13125,
            13125 until 15000
        ).inOrder()
    }

    @Test
    fun `should limit batch size to 2000 for 20000 candidates with 8 processors`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 8)

        // When
        val result = useCase(20000)

        // Then
        // Each processor would get 20000/8 = 2500, exceeding MAX_BATCH_SIZE
        // Need ceiling(20000/(8*2000)) = 2 batches per processor = 16 total batches
        // Base size = 20000/16 = 1250, remainder = 0
        assertThat(result).containsExactly(
            0 until 1250,
            1250 until 2500,
            2500 until 3750,
            3750 until 5000,
            5000 until 6250,
            6250 until 7500,
            7500 until 8750,
            8750 until 10000,
            10000 until 11250,
            11250 until 12500,
            12500 until 13750,
            13750 until 15000,
            15000 until 16250,
            16250 until 17500,
            17500 until 18750,
            18750 until 20000
        ).inOrder()
    }

    @Test
    fun `should limit batch size to 2000 for 50000 candidates with 4 processors`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 4)

        // When
        val result = useCase(50000)

        // Then
        // Need ceiling(50000/(4*2000)) = 7 batches per processor = 28 total batches
        // Base size = 50000/28 = 1785, remainder = 20
        // First 20 batches get size 1786, remaining 8 batches get size 1785
        assertThat(result).containsExactly(
            0 until 1786,
            1786 until 3572,
            3572 until 5358,
            5358 until 7144,
            7144 until 8930,
            8930 until 10716,
            10716 until 12502,
            12502 until 14288,
            14288 until 16074,
            16074 until 17860,
            17860 until 19646,
            19646 until 21432,
            21432 until 23218,
            23218 until 25004,
            25004 until 26790,
            26790 until 28576,
            28576 until 30362,
            30362 until 32148,
            32148 until 33934,
            33934 until 35720,
            35720 until 37505,
            37505 until 39290,
            39290 until 41075,
            41075 until 42860,
            42860 until 44645,
            44645 until 46430,
            46430 until 48215,
            48215 until 50000
        ).inOrder()
    }

    @Test
    fun `should limit batch size to 2000 for 100000 candidates with 8 processors`() {
        // Given
        val useCase = CreateRangesUseCase(availableProcessors = 8)

        // When
        val result = useCase(100000)

        // Then
        // Need ceiling(100000/(8*2000)) = 7 batches per processor = 56 total batches
        // Base size = 100000/56 = 1785, remainder = 40
        // First 40 batches get size 1786, remaining 16 batches get size 1785
        assertThat(result).containsExactly(
            0 until 1786,
            1786 until 3572,
            3572 until 5358,
            5358 until 7144,
            7144 until 8930,
            8930 until 10716,
            10716 until 12502,
            12502 until 14288,
            14288 until 16074,
            16074 until 17860,
            17860 until 19646,
            19646 until 21432,
            21432 until 23218,
            23218 until 25004,
            25004 until 26790,
            26790 until 28576,
            28576 until 30362,
            30362 until 32148,
            32148 until 33934,
            33934 until 35720,
            35720 until 37506,
            37506 until 39292,
            39292 until 41078,
            41078 until 42864,
            42864 until 44650,
            44650 until 46436,
            46436 until 48222,
            48222 until 50008,
            50008 until 51794,
            51794 until 53580,
            53580 until 55366,
            55366 until 57152,
            57152 until 58938,
            58938 until 60724,
            60724 until 62510,
            62510 until 64296,
            64296 until 66082,
            66082 until 67868,
            67868 until 69654,
            69654 until 71440,
            71440 until 73225,
            73225 until 75010,
            75010 until 76795,
            76795 until 78580,
            78580 until 80365,
            80365 until 82150,
            82150 until 83935,
            83935 until 85720,
            85720 until 87505,
            87505 until 89290,
            89290 until 91075,
            91075 until 92860,
            92860 until 94645,
            94645 until 96430,
            96430 until 98215,
            98215 until 100000
        ).inOrder()
    }
}
