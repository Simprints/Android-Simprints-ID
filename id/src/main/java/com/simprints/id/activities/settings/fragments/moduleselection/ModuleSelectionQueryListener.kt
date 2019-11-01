package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.appcompat.widget.SearchView
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.utils.QueryFilter

class ModuleSelectionQueryListener(
    private val adapter: ModuleAdapter,
    private val modules: List<Module>,
    private val searchResultCallback: SearchResultCallback
) : SearchView.OnQueryTextListener {

    private val queryFilter: QueryFilter<Module> = ModuleQueryFilter()

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        val filteredList = queryFilter.getFilteredList(modules, newText)

        if (filteredList.isEmpty())
            searchResultCallback.onNothingFound()
        else
            searchResultCallback.onResultsFound()

        adapter.submitList(filteredList)
        return false
    }

    interface SearchResultCallback {
        fun onNothingFound()
        fun onResultsFound()
    }

}
