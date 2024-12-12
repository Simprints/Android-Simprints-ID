package com.simprints.infra.license.models.comparators

import kotlin.collections.indices
import kotlin.collections.mapNotNull
import kotlin.text.split
import kotlin.text.toIntOrNull

/**
 * Performs version comparison part by part from major to patch.
 *
 * Both compared versions must have same amount of segments.
 * Any differences in version format short-circuit the comparison to return 0.
 */
internal class SemanticVersionComparator : Comparator<String> {
    override fun compare(
        leftVersion: String,
        rightVersion: String,
    ): Int {
        val leftParts = leftVersion.split(".").mapNotNull { it.toIntOrNull() }
        val rightParts = rightVersion.split(".").mapNotNull { it.toIntOrNull() }

        if (leftParts.size != rightParts.size) {
            // If version are different format, comparison is not possible
            return 0
        }

        for (i in leftParts.indices) {
            if (leftParts[i] == rightParts[i]) {
                continue
            } else {
                return leftParts[i].compareTo(rightParts[i])
            }
        }
        return 0
    }
}
