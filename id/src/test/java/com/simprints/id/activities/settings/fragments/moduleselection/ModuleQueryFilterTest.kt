package com.simprints.id.activities.settings.fragments.moduleselection

import com.google.common.truth.Truth.assertThat
import com.simprints.id.moduleselection.model.Module
import org.junit.Test

class ModuleQueryFilterTest {

    private val items = listOf(
        Module("A", false),
        Module("B", true),
        Module("C", false),
        Module("D", false),
        Module("E", false),
        Module("F", false),
        Module("G", false),
        Module("H", false),
        Module("I", false),
        Module("Alpha", false)
    )
    private val filter = ModuleQueryFilter()

    @Test
    fun withEmptyQuery_shouldReturnEmptyList() {
        val query = ""
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEmpty()
    }

    @Test
    fun withExactQuery_shouldReturnCorrectResult() {
        val query = "D"
        val expected = listOf(
            Module("D", false)
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withLowercaseQuery_shouldReturnCorrectResult() {
        val query = "c"
        val expected = listOf(
            Module("C", false)
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withInexactQuery_shouldReturnAllPossibleResults() {
        val query = "a"
        val expected = listOf(
            Module("A", false),
            Module("Alpha", false)
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun shouldNotReturnSelectedModules() {
        val query = "b"
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEmpty()
    }

}
