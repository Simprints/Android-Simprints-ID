package com.simprints.id.activities.settings.fragments.moduleselection.tools

import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.utils.QueryFilter
import me.xdrop.fuzzywuzzy.FuzzySearch

class ModuleQueryFilter : QueryFilter<Module> {

    override fun getFilteredList(items: List<Module>, query: String?): List<Module> {
        if (query.isNullOrEmpty() || query.isBlank() || items.isEmpty())
            return items

        val moduleNames = FuzzySearch.extractAll(query, items.map { it.name })
            .filter { it.score > 50 }
            .sortedByDescending { it.score }
            .map { it.string }

        return items.filter { module ->
            moduleNames.contains(module.name)
        }
    }

}
