package com.simprints.matcher.usecases

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CreateRangesUseCaseTest {
    private val useCase = CreateRangesUseCase()

    @Test
    fun `should create correct ranges when numCandidates equals numConsumers`() {
        // When
        val result = useCase(5, 5)

        // Then
        assertThat(result).containsExactly(
            0..0,
            1..1,
            2..2,
            3..3,
            4..4
        ).inOrder()
    }

    @Test
    fun `should create correct ranges when numCandidates is greater than numConsumers`() {
        // When
        val result = useCase(10, 3)

        // Then
        assertThat(result).containsExactly(
            0..3,
            4..6,
            7..9
        ).inOrder()
    }

    @Test
    fun `should handle single item`() {
        // When
        val result = useCase(1, 4)

        // Then
        assertThat(result).containsExactly(0 until 1).inOrder()
    }

    @Test
    fun `should handle totalCount equal to MAX_BATCH_SIZE`() {
        // When
        val result = useCase(2000, 1)

        // Then
        assertThat(result).containsExactly(0 until 2000).inOrder()
    }

    @Test
    fun `should handle batch sizes that are exactly MAX_BATCH_SIZE`() {
        // When
        val result = useCase(4000, 2)

        // Then
        assertThat(result).containsExactly(
            0 until 2000,
            2000 until 4000
        ).inOrder()
    }

    @Test
    fun `should create correct ranges when numCandidates is less than numConsumers`() {
        // When
        val result = useCase(3, 5)

        // Then
        assertThat(result).containsExactly(
            0..0,
            1..1,
            2..2
        ).inOrder()
    }

    @Test
    fun `should create correct ranges with uneven distribution`() {
        // When
        val result = useCase(11, 3)

        // Then
        assertThat(result).containsExactly(
            0..3,
            4..7,
            8..10
        ).inOrder()
    }

    @Test
    fun `should create empty list when numCandidates is zero`() {
        // When
        val result = useCase(0, 5)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should create single range when numConsumers is one`() {
        // When
        val result = useCase(10, 1)

        // Then
        assertThat(result).containsExactly(0..9)
    }

    @Test
    fun `should handle large numbers correctly`() {
        // When
        val result = useCase(1000, 4)

        // Then
        assertThat(result).containsExactly(
            0..249,
            250..499,
            500..749,
            750..999
        ).inOrder()
    }

    @Test
    fun `should handle 2500 candidates with 4 processors`() {
        // When
        val result = useCase(2500, 4)

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
        // When
        val result = useCase(5000, 4)

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
        // When
        val result = useCase(10000, 8)

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
        // When
        val result = useCase(15000, 4)

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
        // When
        val result = useCase(20000, 8)

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
        // When
        val result = useCase(50000, 4)

        // Then
        // Need ceiling(50000/(4*2000)) = 7 batches per processor = 28 total batches
        // Base size = 50000/28 = 1785, remainder = 20
        // First 20 batches get size 1786, remaining 8 batches get size 1785
        val expected = mutableListOf<IntRange>()
        var start = 0
        for (i in 0 until 28) {
            val batchSize = 1785 + if (i < 20) 1 else 0
            val end = start + batchSize
            expected.add(start until end)
            start = end
        }
        assertThat(result).containsExactlyElementsIn(expected).inOrder()
    }

    @Test
    fun `should limit batch size to 2000 for 100000 candidates with 8 processors`() {
        // When
        val result = useCase(100000, 8)

        // Then
        // Need ceiling(100000/(8*2000)) = 7 batches per processor = 56 total batches
        // Base size = 100000/56 = 1785, remainder = 40
        // First 40 batches get size 1786, remaining 16 batches get size 1785
        val expected = mutableListOf<IntRange>()
        var start = 0
        for (i in 0 until 56) {
            val batchSize = 1785 + if (i < 40) 1 else 0
            val end = start + batchSize
            expected.add(start until end)
            start = end
        }
        assertThat(result).containsExactlyElementsIn(expected).inOrder()
    }
}
