package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools

import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module

internal interface ChipClickListener {
    fun onChipClick(module: Module)
}
