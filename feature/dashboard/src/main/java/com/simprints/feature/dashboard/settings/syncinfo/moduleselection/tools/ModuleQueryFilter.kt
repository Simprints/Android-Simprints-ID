package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools

import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import com.simprints.feature.dashboard.tools.fuzzySearch

internal class ModuleQueryFilter {
    fun getFilteredList(
        items: List<Module>,
        query: String?,
    ): List<Module> = if (isRelevantQuery(query) && items.isNotEmpty()) {
        items.fuzzySearch(query, { it.name.value })
    } else {
        items
    }

    private fun isRelevantQuery(query: String?): Boolean = !query.isNullOrBlank()
}
