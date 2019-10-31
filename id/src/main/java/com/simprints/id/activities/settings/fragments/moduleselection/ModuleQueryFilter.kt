package com.simprints.id.activities.settings.fragments.moduleselection

import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.utils.QueryFilter
import java.util.*

class ModuleQueryFilter : QueryFilter<Module> {

    override fun filter(
        items: List<Module>,
        query: String?,
        callback: QueryFilter.SearchResultCallback?
    ): List<Module> {
        val defaultLocale = Locale.getDefault()
        val result = query?.toLowerCase(defaultLocale)?.let { lowercaseQuery ->
            items.filter {
                it.name.toLowerCase(defaultLocale).contains(lowercaseQuery)
                // TODO: add !it.isSelected to filter once "selected modules" area is implemented
            }
        } ?: emptyList()

        if (result.isEmpty())
            callback?.onNothingFound()
        else
            callback?.onResultsFound()

        return result
    }

}
