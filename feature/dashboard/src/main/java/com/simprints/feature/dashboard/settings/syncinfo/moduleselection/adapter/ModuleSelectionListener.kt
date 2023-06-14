package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.adapter

import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module

internal interface ModuleSelectionListener {
    fun onModuleSelected(module: Module)
}
