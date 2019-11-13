package com.simprints.id.activities.settings.fragments.moduleselection.tools

import com.simprints.id.moduleselection.model.Module
import com.simprints.id.tools.utils.QueryFilter
import me.xdrop.fuzzywuzzy.FuzzySearch

class ModuleQueryFilter : QueryFilter<Module> {

    override fun getFilteredList(items: List<Module>, query: String?): List<Module> {
        return if (isRelevantQuery(query) && items.isNotEmpty())
            items.extractResults(query)
        else
            items
    }

    private fun isRelevantQuery(query: String?): Boolean {
        return query != null && query.isNotEmpty() && query.isNotBlank()
    }

    private fun List<Module>.extractResults(query: String?): List<Module> {
        return FuzzySearch.extractAll(
            query, this, { it.name }, MATCHING_SCORE_THRESHOLD
        ).sortedByDescending { it.score }.map { it.referent }
    }

    companion object {
        private const val MATCHING_SCORE_THRESHOLD = 50
    }

}
