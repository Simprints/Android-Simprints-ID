package com.simprints.feature.dashboard.settings.syncinfo.moduleselection

import androidx.appcompat.widget.SearchView
import androidx.lifecycle.MutableLiveData
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools.ModuleQueryFilter

internal class ModuleSelectionQueryListener(private val modules: List<Module>) :
    SearchView.OnQueryTextListener {

    val searchResults = MutableLiveData<List<Module>>()

    private val queryFilter = ModuleQueryFilter()

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        searchResults.value = queryFilter.getFilteredList(modules, newText)
        return false
    }

}
