package com.simprints.infra.matching.usecase

import com.simprints.core.domain.sample.MatchConfidence
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class MatchResultSet(
    private val maxSize: Int = MAX_RESULTS,
) {
    private val lowestConfidence = AtomicReference(0f)
    private val lock = ReentrantLock()

    private val skipListSet = ConcurrentSkipListSet(
        compareByDescending<MatchConfidence> { it.confidence }.thenByDescending { it.subjectId },
    )

    fun add(element: MatchConfidence): MatchResultSet {
        // Use a lock to ensure thread safety during the entire add operation
        lock.withLock {
            // Only perform this optimization when we know the set is at max capacity
            if (skipListSet.size >= maxSize && lowestConfidence.get() > element.confidence) {
                // skip adding if the set is full and the last element has higher confidence than the current element
                return this
            }

            skipListSet.add(element)
            if (skipListSet.size > maxSize) {
                skipListSet.pollLast()

                // Now that the set is full, we can skip adding elements
                // with confidence lower than the current lowest
                lowestConfidence.set(skipListSet.last().confidence)
            }
            return this
        }
    }

    fun addAll(elements: MatchResultSet): MatchResultSet {
        elements.skipListSet.forEach { add(it) }
        return this
    }

    fun toList(): List<MatchConfidence> = skipListSet.toList()

    companion object {
        /**
         * Default max size of the result set.
         */
        private const val MAX_RESULTS = 10
    }
}
