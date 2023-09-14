package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import org.junit.Test

class ModuleQueryFilterTest {

    private val items = listOf(
        Module("Abama".asTokenizedRaw(), false),
        Module("Abama Dawet".asTokenizedRaw(), false),
        Module("Achura Mazegaja".asTokenizedRaw(), false),
        Module("Bangladesh rocks".asTokenizedRaw(), false),
        Module("Dache Gofara".asTokenizedRaw(), false),
        Module("Gara Goda".asTokenizedRaw(), false),
        Module("Gurumo Koysha".asTokenizedRaw(), false),
        Module("Hajo Salata".asTokenizedRaw(), false),
        Module("Legama".asTokenizedRaw(), false),
        Module("Madagascar".asTokenizedRaw(), false),
        Module("Tadisa".asTokenizedRaw(), false),
        Module("Wakanda".asTokenizedRaw(), false)
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
            Module(name = "Legama".asTokenizedRaw(), isSelected = false),
            Module(name = "Achura Mazegaja".asTokenizedRaw(), isSelected = false),
            Module(name = "Abama".asTokenizedRaw(), isSelected = false)
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withLowercaseQuery_shouldReturnCorrectResult() {
        val query = "legama"
        val expected = listOf(
            Module(name = "Legama".asTokenizedRaw(), isSelected = false),
            Module(name = "Achura Mazegaja".asTokenizedRaw(), isSelected = false),
            Module(name = "Abama".asTokenizedRaw(), isSelected = false)
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withInexactQuery_shouldReturnAllPossibleResults() {
        val query = "abama"
        val expected = listOf(
            Module("Abama".asTokenizedRaw(), false),
            Module("Abama Dawet".asTokenizedRaw(), false),
            Module("Legama".asTokenizedRaw(), false),
            Module("Achura Mazegaja".asTokenizedRaw(), false),
            Module("Hajo Salata".asTokenizedRaw(), false),
            Module("Wakanda".asTokenizedRaw(), false)
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withFuzzyQuery_shouldReturnAllPossibleResults() {
        val query = "binglodosh"
        val expected = listOf(
            Module("Bangladesh rocks".asTokenizedRaw(), false)
        )

        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

}
