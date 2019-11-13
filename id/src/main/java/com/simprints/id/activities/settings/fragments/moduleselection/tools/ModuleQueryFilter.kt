package com.simprints.id.activities.settings.fragments.moduleselection.tools

import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.extensions.fuzzySearch
import com.simprints.id.tools.utils.QueryFilter

class ModuleQueryFilter : QueryFilter<Module> {

    override fun getFilteredList(items: List<Module>, query: String?): List<Module> {
        return if (isRelevantQuery(query) && items.isNotEmpty())
            items.fuzzySearch(query, { it.name })
        else
            items
    }

    private fun isRelevantQuery(query: String?): Boolean {
        return query != null && query.isNotEmpty() && query.isNotBlank()
    }

}
