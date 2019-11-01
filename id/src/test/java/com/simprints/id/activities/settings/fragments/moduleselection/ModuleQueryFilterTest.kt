package com.simprints.id.activities.settings.fragments.moduleselection

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.utils.QueryFilter
import com.simprints.testtools.common.syntax.mock
import org.junit.Test

class ModuleQueryFilterTest {

    private val items = listOf(
        Module("A", false),
        Module("B", false),
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
    fun withNullQuery_shouldReturnEmptyList() {
        val query: String? = null
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
        val query = "b"
        val expected = listOf(
            Module("B", false)
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
    fun withNoResultsFound_shouldTriggerCallback() {
        val query = "z"
        val callback = mock<QueryFilter.SearchResultCallback>()

        filter.getFilteredList(items, query, callback)

        verify(callback).onNothingFound()
    }

    @Test
    fun withResultsFound_shouldTriggerCallback() {
        val query = "c"
        val callback = mock<QueryFilter.SearchResultCallback>()

        filter.getFilteredList(items, query, callback)

        verify(callback).onResultsFound()
    }

}
