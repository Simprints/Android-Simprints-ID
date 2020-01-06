package com.simprints.id.activities.settings.fragments.moduleselection.tools

import com.google.common.truth.Truth.assertThat
import com.simprints.id.moduleselection.model.Module
import org.junit.Test

class ModuleQueryFilterTest {

    private val items = listOf(
        Module("Abama", false),
        Module("Abama Dawet", false),
        Module("Achura Mazegaja", false),
        Module("Bangladesh rocks", false),
        Module("Dache Gofara", false),
        Module("Gara Goda", false),
        Module("Gurumo Koysha", false),
        Module("Hajo Salata", false),
        Module("Legama", false),
        Module("Madagascar", false),
        Module("Tadisa", false),
        Module("Wakanda", false)
    )
    private val filter = ModuleQueryFilter()

    @Test
    fun withEmptyQuery_shouldReturnOriginalList() {
        val query = ""
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(items)
    }

    @Test
    fun withBlankQuery_shouldReturnOriginalList() {
        val query = " "
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(items)
    }

    @Test
    fun withNullQuery_shouldReturnOriginalList() {
        val query: String? = null
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(items)
    }

    @Test
    fun withEmptySourceList_shouldReturnOriginalList() {
        val query = "I\'ll have two number 9, a number 9 large, a number 6 with extra dip, " +
            "a number 7, two number 45, one with cheese and a large soda"
        val list = emptyList<Module>()
        val actual = filter.getFilteredList(list, query)

        assertThat(actual).isEmpty()
    }

    @Test
    fun withExactQuery_shouldReturnCorrectResult() {
        val query = "Legama"
        val expected = listOf(
            Module(name="Legama", isSelected=false),
            Module(name="Achura Mazegaja", isSelected=false),
            Module(name="Abama", isSelected=false)
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withLowercaseQuery_shouldReturnCorrectResult() {
        val query = "legama"
        val expected = listOf(
            Module(name="Legama", isSelected=false),
            Module(name="Achura Mazegaja", isSelected=false),
            Module(name="Abama", isSelected=false)
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withInexactQuery_shouldReturnAllPossibleResults() {
        val query = "abama"
        val expected = listOf(
            Module("Abama", false),
            Module("Abama Dawet", false),
            Module("Legama", false),
            Module("Achura Mazegaja", false),
            Module("Hajo Salata", false),
            Module("Madagascar", false),
            Module("Wakanda", false)
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withFuzzyQuery_shouldReturnAllPossibleResults() {
        val query = "binglodosh"
        val expected = listOf(
            Module("Bangladesh rocks", false)
        )

        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

}
