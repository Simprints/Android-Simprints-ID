package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.appcompat.widget.SearchView
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.moduleselection.model.Module

class ModuleSelectionQueryListener(
    private val adapter: ModuleAdapter,
    private val modules: List<Module>
) : SearchView.OnQueryTextListener {

    private val queryFilter = ModuleQueryFilter()

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        val filteredList = queryFilter.filter(modules, newText)
        adapter.submitList(filteredList)
        return false
    }

}
