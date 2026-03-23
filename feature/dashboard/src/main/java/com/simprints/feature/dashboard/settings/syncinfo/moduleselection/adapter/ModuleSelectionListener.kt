package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.adapter

import com.simprints.infra.eventsync.module.SelectableModule

internal interface ModuleSelectionListener {
    fun onModuleSelected(module: SelectableModule)
}
