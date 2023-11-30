package com.simprints.matcher.usecases

import com.simprints.matcher.MatchResultItem
import java.util.TreeSet

internal class MatchResultSet<T : MatchResultItem>(
    private val maxSize: Int,
) {

    private var lowestConfidence: Float = Float.MIN_VALUE

    private val treeSet = TreeSet { o1: T, o2: T ->
        // Reverse order for descending sort
        -1 * o1.confidence.compareTo(o2.confidence)
    }

    fun add(element: T): MatchResultSet<T> {
        if (lowestConfidence > element.confidence) {
            // skip adding if the last element is greater than the current element
            return this
        }

        treeSet.add(element)
        if (treeSet.size > maxSize) {
            treeSet.pollLast()

            // Not that the set is full, we can skip adding elements
            // with confidence lower than the current lowest
            lowestConfidence = treeSet.last().confidence
        }
        return this
    }

    fun addAll(elements: MatchResultSet<T>): MatchResultSet<T> {
        elements.treeSet.forEach { add(it) }
        return this
    }

    fun toList(): List<T> = treeSet.toList()
}
