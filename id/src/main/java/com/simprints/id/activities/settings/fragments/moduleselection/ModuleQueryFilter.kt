package com.simprints.id.activities.settings.fragments.moduleselection

import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.utils.QueryFilter

class ModuleQueryFilter : QueryFilter<Module> {

    override fun getFilteredList(
        items: List<Module>,
        query: String?,
        callback: QueryFilter.SearchResultCallback?
    ): List<Module> {
        val result = query?.let { searchTerm ->
            items.filter { module ->
                module.name.contains(searchTerm, ignoreCase = true)
                // TODO: add !module.isSelected to filter once "selected modules" area is implemented
            }
        } ?: emptyList()

        if (result.isEmpty())
            callback?.onNothingFound()
        else
            callback?.onResultsFound()

        return result
    }

}
