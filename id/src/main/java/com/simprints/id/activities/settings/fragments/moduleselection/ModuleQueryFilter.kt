package com.simprints.id.activities.settings.fragments.moduleselection

import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.utils.QueryFilter
import me.xdrop.fuzzywuzzy.FuzzySearch

class ModuleQueryFilter : QueryFilter<Module> {

    override fun getFilteredList(items: List<Module>, query: String?): List<Module> {
        val limit = 5 // TODO: check if it's really necessary
        val searchResults = FuzzySearch.extractTop(query, items.map { it.name }, limit).apply {
            sortByDescending { it.score }
        }

        val moduleNames = searchResults.map { it.string }

        return items.filter { module ->
            moduleNames.contains(module.name)
        }
    }

}
