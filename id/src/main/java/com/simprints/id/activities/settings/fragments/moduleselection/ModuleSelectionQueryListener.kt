package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.appcompat.widget.SearchView
import com.simprints.id.activities.settings.fragments.moduleselection.adapter.ModuleAdapter
import com.simprints.id.moduleselection.model.Module

class ModuleSelectionQueryListener(
    private val adapter: ModuleAdapter,
    private val modules: List<Module>
) : SearchView.OnQueryTextListener {

    override fun onQueryTextSubmit(query: String?) = false

    override fun onQueryTextChange(newText: String?): Boolean {
        adapter.filter(modules, newText)
        return false
    }

}
