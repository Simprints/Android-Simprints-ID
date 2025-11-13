package com.simprints.infra.matching.usecase

import com.google.common.truth.Truth.*
import com.simprints.infra.matching.FingerprintMatchResult
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MatchResultSetTest {
    @Test
    fun `Stores results sorted descending by confidence up to the limit`() {
        val set = MatchResultSet<FingerprintMatchResult.Item>(3)

        set.add(FingerprintMatchResult.Item("4", 0.4f))
        set.add(FingerprintMatchResult.Item("1", 0.1f))
        set.add(FingerprintMatchResult.Item("3", 0.3f))
        set.add(FingerprintMatchResult.Item("3", 0.1f))
        set.add(FingerprintMatchResult.Item("2", 0.2f))

        assertThat(set.toList()).isEqualTo(
            listOf(
                FingerprintMatchResult.Item("4", 0.4f),
                FingerprintMatchResult.Item("3", 0.3f),
                FingerprintMatchResult.Item("2", 0.2f),
            ),
        )
    }

    @Test
    fun `Merges sets preserving the total limit`() {
        val setOne = MatchResultSet<FingerprintMatchResult.Item>(2)
        setOne.add(FingerprintMatchResult.Item("1", 0.1f))
        setOne.add(FingerprintMatchResult.Item("3", 0.3f))

        val setTwo = MatchResultSet<FingerprintMatchResult.Item>(2)
        setTwo.add(FingerprintMatchResult.Item("2", 0.2f))
        setTwo.add(FingerprintMatchResult.Item("4", 0.4f))

        val set = MatchResultSet<FingerprintMatchResult.Item>(3)
        set.addAll(setOne)
        set.addAll(setTwo)

        assertThat(set.toList()).isEqualTo(
            listOf(
                FingerprintMatchResult.Item("4", 0.4f),
                FingerprintMatchResult.Item("3", 0.3f),
                FingerprintMatchResult.Item("2", 0.2f),
            ),
        )
    }

    // On equal confidence scores sort by id
    @Test
    fun `Stores results sorted descending by confidence and id`() {
        val set = MatchResultSet<FingerprintMatchResult.Item>(3)

        set.add(FingerprintMatchResult.Item("4", 0.4f))
        set.add(FingerprintMatchResult.Item("1", 0.4f))
        set.add(FingerprintMatchResult.Item("3", 0.3f))
        set.add(FingerprintMatchResult.Item("2", 0.3f))

        assertThat(set.toList()).isEqualTo(
            listOf(
                FingerprintMatchResult.Item("4", 0.4f),
                FingerprintMatchResult.Item("1", 0.4f),
                FingerprintMatchResult.Item("3", 0.3f),
            ),
        )
    }

    @Test
    fun `Concurrent add operations maintain thread safety`() {
        val set = MatchResultSet<FingerprintMatchResult.Item>(5)
        val threadCount = 10
        val elementsPerThread = 20
        val latch = CountDownLatch(1)

        val executor = Executors.newFixedThreadPool(threadCount)

        // Submit tasks to add items concurrently from multiple threads
        repeat(threadCount) { threadIndex ->
            executor.submit {
                try {
                    // Wait for all threads to be ready
                    latch.await()

                    // Each thread adds its own batch of elements
                    repeat(elementsPerThread) { i ->
                        val confidence = (threadIndex * elementsPerThread + i) / 100f
                        set.add(FingerprintMatchResult.Item("T$threadIndex-$i", confidence))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Release all threads simultaneously
        latch.countDown()

        // Shutdown executor and wait for completion
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)

        // Verify results
        val results = set.toList()

        // Should have exactly 5 items (maxSize)
        assertThat(results.size).isEqualTo(5)

        // Should be sorted by confidence descending
        for (i in 0 until results.size - 1) {
            assertThat(results[i].confidence).isAtLeast(results[i + 1].confidence)
        }

        // Verify the highest confidence item is at the top
        assertThat(results[0].confidence).isEqualTo(1.99f)
    }

    @Test
    fun `Concurrent addAll operations maintain thread safety`() {
        val targetSet = MatchResultSet<FingerprintMatchResult.Item>(5)
        val threadCount = 5
        val latch = CountDownLatch(1)

        // Create source sets with different items
        val sourceSets = List(threadCount) { threadIndex ->
            MatchResultSet<FingerprintMatchResult.Item>(3).apply {
                repeat(5) { i ->
                    val confidence = 0.5f + (threadIndex * 5 + i) / 100f
                    add(FingerprintMatchResult.Item("S$threadIndex-$i", confidence))
                }
            }
        }

        val executor = Executors.newFixedThreadPool(threadCount)

        // Submit tasks to merge sets concurrently
        repeat(threadCount) { threadIndex ->
            executor.submit {
                try {
                    latch.await()
                    targetSet.addAll(sourceSets[threadIndex])
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Release all threads simultaneously
        latch.countDown()

        // Shutdown executor and wait for completion
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)

        // Verify results
        val results = targetSet.toList()

        // Should have exactly 5 items (maxSize)
        assertThat(results.size).isEqualTo(5)

        // Should be sorted by confidence descending
        for (i in 0 until results.size - 1) {
            assertThat(results[i].confidence).isAtLeast(results[i + 1].confidence)
        }

        // Verify the highest confidence item is at the top
        assertThat(results[0].confidence).isEqualTo(0.74f)
    }

    @Test
    fun `addAll correctly filters elements with lower confidence than current minimum`() {
        val set = MatchResultSet<FingerprintMatchResult.Item>(3)

        // Add higher confidence items first to fill the set
        set.add(FingerprintMatchResult.Item("A", 0.8f))
        set.add(FingerprintMatchResult.Item("B", 0.7f))
        set.add(FingerprintMatchResult.Item("C", 0.6f))

        // Try to add a new set with lower confidence items
        val lowerSet = MatchResultSet<FingerprintMatchResult.Item>(3)
        lowerSet.add(FingerprintMatchResult.Item("D", 0.5f))
        lowerSet.add(FingerprintMatchResult.Item("E", 0.4f))
        lowerSet.add(FingerprintMatchResult.Item("F", 0.3f))

        // Add one higher item to verify it still gets added
        lowerSet.add(FingerprintMatchResult.Item("G", 0.9f))

        set.addAll(lowerSet)

        // Verify results
        val results = set.toList()

        assertThat(results).hasSize(3)
        assertThat(results[0].confidence).isEqualTo(0.9f)
        assertThat(results[1].confidence).isEqualTo(0.8f)
        assertThat(results[2].confidence).isEqualTo(0.7f)
    }
}
