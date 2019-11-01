package com.simprints.id.activities.settings.fragments.moduleselection

import com.nhaarman.mockitokotlin2.verify
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.moduleselection.model.Module
import com.simprints.testtools.common.syntax.mock
import org.junit.Test

class ModuleSelectionQueryListenerTest {

    private val modules = listOf(
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
    private val adapter = mock<ModuleAdapter>()
    private val callback = mock<ModuleSelectionQueryListener.SearchResultCallback>()
    private val listener = ModuleSelectionQueryListener(adapter, modules, callback)

    @Test
    fun withNoResultsFound_shouldTriggerCallback() {
        val query = "z"
        listener.onQueryTextChange(query)
        verify(callback).onNothingFound()
    }

    @Test
    fun withResultsFound_shouldTriggerCallback() {
        val query = "c"
        listener.onQueryTextChange(query)
        verify(callback).onResultsFound()
    }

}
