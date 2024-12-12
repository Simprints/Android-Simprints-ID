package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import org.junit.Test

class ModuleQueryFilterTest {
    private val items = listOf(
        Module("Abama".asTokenizableRaw(), false),
        Module("Abama Dawet".asTokenizableRaw(), false),
        Module("Achura Mazegaja".asTokenizableRaw(), false),
        Module("Bangladesh rocks".asTokenizableRaw(), false),
        Module("Dache Gofara".asTokenizableRaw(), false),
        Module("Gara Goda".asTokenizableRaw(), false),
        Module("Gurumo Koysha".asTokenizableRaw(), false),
        Module("Hajo Salata".asTokenizableRaw(), false),
        Module("Legama".asTokenizableRaw(), false),
        Module("Madagascar".asTokenizableRaw(), false),
        Module("Tadisa".asTokenizableRaw(), false),
        Module("Wakanda".asTokenizableRaw(), false),
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
            Module(name = "Legama".asTokenizableRaw(), isSelected = false),
            Module(name = "Achura Mazegaja".asTokenizableRaw(), isSelected = false),
            Module(name = "Abama".asTokenizableRaw(), isSelected = false),
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withLowercaseQuery_shouldReturnCorrectResult() {
        val query = "legama"
        val expected = listOf(
            Module(name = "Legama".asTokenizableRaw(), isSelected = false),
            Module(name = "Achura Mazegaja".asTokenizableRaw(), isSelected = false),
            Module(name = "Abama".asTokenizableRaw(), isSelected = false),
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withInexactQuery_shouldReturnAllPossibleResults() {
        val query = "abama"
        val expected = listOf(
            Module("Abama".asTokenizableRaw(), false),
            Module("Abama Dawet".asTokenizableRaw(), false),
            Module("Legama".asTokenizableRaw(), false),
            Module("Achura Mazegaja".asTokenizableRaw(), false),
            Module("Hajo Salata".asTokenizableRaw(), false),
            Module("Wakanda".asTokenizableRaw(), false),
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withFuzzyQuery_shouldReturnAllPossibleResults() {
        val query = "binglodosh"
        val expected = listOf(
            Module("Bangladesh rocks".asTokenizableRaw(), false),
        )

        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }
}
