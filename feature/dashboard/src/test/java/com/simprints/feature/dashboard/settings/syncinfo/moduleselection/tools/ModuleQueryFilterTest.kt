package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.eventsync.module.SelectableModule
import org.junit.Test

class ModuleQueryFilterTest {
    private val items = listOf(
        SelectableModule("Abama".asTokenizableRaw(), false),
        SelectableModule("Abama Dawet".asTokenizableRaw(), false),
        SelectableModule("Achura Mazegaja".asTokenizableRaw(), false),
        SelectableModule("Bangladesh rocks".asTokenizableRaw(), false),
        SelectableModule("Dache Gofara".asTokenizableRaw(), false),
        SelectableModule("Gara Goda".asTokenizableRaw(), false),
        SelectableModule("Gurumo Koysha".asTokenizableRaw(), false),
        SelectableModule("Hajo Salata".asTokenizableRaw(), false),
        SelectableModule("Legama".asTokenizableRaw(), false),
        SelectableModule("Madagascar".asTokenizableRaw(), false),
        SelectableModule("Tadisa".asTokenizableRaw(), false),
        SelectableModule("Wakanda".asTokenizableRaw(), false),
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
        val list = emptyList<SelectableModule>()
        val actual = filter.getFilteredList(list, query)

        assertThat(actual).isEmpty()
    }

    @Test
    fun withExactQuery_shouldReturnCorrectResult() {
        val query = "Legama"
        val expected = listOf(
            SelectableModule(name = "Legama".asTokenizableRaw(), isSelected = false),
            SelectableModule(name = "Achura Mazegaja".asTokenizableRaw(), isSelected = false),
            SelectableModule(name = "Abama".asTokenizableRaw(), isSelected = false),
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withLowercaseQuery_shouldReturnCorrectResult() {
        val query = "legama"
        val expected = listOf(
            SelectableModule(name = "Legama".asTokenizableRaw(), isSelected = false),
            SelectableModule(name = "Achura Mazegaja".asTokenizableRaw(), isSelected = false),
            SelectableModule(name = "Abama".asTokenizableRaw(), isSelected = false),
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withInexactQuery_shouldReturnAllPossibleResults() {
        val query = "abama"
        val expected = listOf(
            SelectableModule("Abama".asTokenizableRaw(), false),
            SelectableModule("Abama Dawet".asTokenizableRaw(), false),
            SelectableModule("Legama".asTokenizableRaw(), false),
            SelectableModule("Achura Mazegaja".asTokenizableRaw(), false),
            SelectableModule("Hajo Salata".asTokenizableRaw(), false),
            SelectableModule("Wakanda".asTokenizableRaw(), false),
        )
        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun withFuzzyQuery_shouldReturnAllPossibleResults() {
        val query = "binglodosh"
        val expected = listOf(
            SelectableModule("Bangladesh rocks".asTokenizableRaw(), false),
        )

        val actual = filter.getFilteredList(items, query)

        assertThat(actual).isEqualTo(expected)
    }
}
