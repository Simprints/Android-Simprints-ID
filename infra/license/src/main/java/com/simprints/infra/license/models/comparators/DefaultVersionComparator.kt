package com.simprints.infra.license.models.comparators

/**
 * Best effort string version comparison without any strict guarantees.
 *
 * Both versions are compared as numbers if possible and fallback to string comparison.
 * Therefore, 10>9 but v9>v10, since the string comparison works character by character.
 */
internal class DefaultVersionComparator() : Comparator<String> {

    override fun compare(p0: String, p1: String): Int {
        val i0 = p0.toIntOrNull()
        val i1 = p1.toIntOrNull()

        return if (i0 != null && i1 != null) {
            i0.compareTo(i1)
        } else {
            p0.compareTo(p1)
        }
    }
}
