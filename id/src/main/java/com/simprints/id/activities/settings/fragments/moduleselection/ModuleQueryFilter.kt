package com.simprints.id.activities.settings.fragments.moduleselection

import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.utils.QueryFilter

class ModuleQueryFilter : QueryFilter<Module> {

    override fun getFilteredList(items: List<Module>, query: String?): List<Module> {
        return query?.let { searchTerm ->
            items.filter { module ->
                module.name.contains(searchTerm, ignoreCase = true)
            }
        } ?: emptyList()
    }

}
