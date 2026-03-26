package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools

import com.simprints.feature.dashboard.tools.fuzzySearch
import com.simprints.infra.eventsync.module.SelectableModule

internal class ModuleQueryFilter {
    fun getFilteredList(
        items: List<SelectableModule>,
        query: String?,
    ): List<SelectableModule> = if (isRelevantQuery(query) && items.isNotEmpty()) {
        items.fuzzySearch(query, { it.name.value })
    } else {
        items
    }

    private fun isRelevantQuery(query: String?): Boolean = !query.isNullOrBlank()
}
