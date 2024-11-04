package com.simprints.infra.license.models.comparators

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SemanticVersionComparatorTest {

    private val comparator = SemanticVersionComparator()

    @Test
    fun `compares single part version correctly`() {
        assertThat(comparator.compare("2", "1")).isGreaterThan(0)
        assertThat(comparator.compare("1", "2")).isLessThan(0)
        assertThat(comparator.compare("1", "1")).isEqualTo(0)
    }

    @Test
    fun `compares two part version correctly`() {
        assertThat(comparator.compare("1.3", "1.1")).isGreaterThan(0)
        assertThat(comparator.compare("2.0", "1.1")).isGreaterThan(0)

        assertThat(comparator.compare("1.1", "1.3")).isLessThan(0)
        assertThat(comparator.compare("1.1", "2.0")).isLessThan(0)

        assertThat(comparator.compare("1.1", "1.1")).isEqualTo(0)
    }

    @Test
    fun `compares three part version correctly`() {
        assertThat(comparator.compare("1.1.3", "1.1.1")).isGreaterThan(0)
        assertThat(comparator.compare("1.2.0", "1.1.1")).isGreaterThan(0)
        assertThat(comparator.compare("2.0.0", "1.1.1")).isGreaterThan(0)

        assertThat(comparator.compare("1.1.1", "1.1.3")).isLessThan(0)
        assertThat(comparator.compare("1.1.1", "1.2.0")).isLessThan(0)
        assertThat(comparator.compare("1.1.1", "2.0.0")).isLessThan(0)

        assertThat(comparator.compare("1.1.1", "1.1.1")).isEqualTo(0)
    }
}
