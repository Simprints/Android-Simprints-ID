package com.simprints.id.activities.settings.fragments.moduleselection

import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.utils.QueryFilter
import me.xdrop.fuzzywuzzy.FuzzySearch

class ModuleQueryFilter : QueryFilter<Module> {

    override fun getFilteredList(items: List<Module>, query: String?): List<Module> {
        val limit = 5 // TODO: check
        val moduleNames = FuzzySearch.extractTop(query, items.map { it.name }, limit)
            .filter { it.score > 50 } // TODO: check
            .sortedByDescending { it.score }
            .map { it.string }

        return items.filter { module ->
            moduleNames.contains(module.name) && !module.isSelected
        }
    }

}
