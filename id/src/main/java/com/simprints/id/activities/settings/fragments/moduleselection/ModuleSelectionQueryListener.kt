package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.appcompat.widget.SearchView
import androidx.lifecycle.MutableLiveData
import com.simprints.id.activities.settings.fragments.moduleselection.tools.ModuleQueryFilter
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.utils.QueryFilter

class ModuleSelectionQueryListener(
    private val modules: List<Module>
) : SearchView.OnQueryTextListener {

    val searchResults = MutableLiveData<List<Module>>()

    private val queryFilter: QueryFilter<Module> = ModuleQueryFilter()

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        searchResults.value = queryFilter.getFilteredList(modules, newText)
        return false
    }

}
