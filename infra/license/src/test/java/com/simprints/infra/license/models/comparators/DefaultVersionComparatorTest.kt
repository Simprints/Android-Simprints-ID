package com.simprints.infra.license.models.comparators

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DefaultVersionComparatorTest {
    private val comparator = DefaultVersionComparator()

    @Test
    fun `correctly compares number strings`() {
        assertThat(comparator.compare("2", "1")).isGreaterThan(0)
        assertThat(comparator.compare("1", "1")).isEqualTo(0)
        assertThat(comparator.compare("1", "2")).isLessThan(0)

        assertThat(comparator.compare("8", "12")).isLessThan(0)
        assertThat(comparator.compare("24", "19")).isGreaterThan(0)
    }

    @Test
    fun `correctly compares strings with equal prefix`() {
        assertThat(comparator.compare("v1", "v1")).isEqualTo(0)
        assertThat(comparator.compare("v2", "v1")).isGreaterThan(0)
        assertThat(comparator.compare("v1", "v2")).isLessThan(0)

        // This is expected since strings are compared by char
        assertThat(comparator.compare("v2", "v12")).isGreaterThan(0)
        assertThat(comparator.compare("v12", "v2")).isLessThan(0)
    }
}
