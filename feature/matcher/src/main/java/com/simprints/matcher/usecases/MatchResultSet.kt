package com.simprints.matcher.usecases

import com.simprints.matcher.MatchResultItem
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicReference

internal class MatchResultSet<T : MatchResultItem>(
    private val maxSize: Int = MAX_RESULTS,
) {
    private val lowestConfidence = AtomicReference(0f)

    private val skipListSet = ConcurrentSkipListSet(
        compareByDescending<T> { it.confidence }.thenByDescending { it.subjectId },
    )

    fun add(element: T): MatchResultSet<T> {
        if (lowestConfidence.get() > element.confidence) {
            // skip adding if the last element is greater than the current element
            return this
        }

        skipListSet.add(element)
        if (skipListSet.size > maxSize) {
            skipListSet.pollLast()

            // Not that the set is full, we can skip adding elements
            // with confidence lower than the current lowest
            lowestConfidence.set(skipListSet.last().confidence)
        }
        return this
    }

    fun addAll(elements: MatchResultSet<T>): MatchResultSet<T> {
        elements.skipListSet.forEach { add(it) }
        return this
    }

    fun toList(): List<T> = skipListSet.toList()

    companion object {
        /**
         * Default max size of the result set.
         */
        private const val MAX_RESULTS = 10
    }
}
