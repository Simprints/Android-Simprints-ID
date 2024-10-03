package com.simprints.infra.license.models.comparators

/**
 * Performs direct string comparison without any strict guarantees.
 */
internal class DefaultVersionComparator() : Comparator<String> {
    override fun compare(p0: String, p1: String) = p0.compareTo(p1)
}
