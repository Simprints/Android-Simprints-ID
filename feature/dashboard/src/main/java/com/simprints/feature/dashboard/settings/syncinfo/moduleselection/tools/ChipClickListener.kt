package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.tools

import com.simprints.infra.eventsync.module.SelectableModule

internal interface ChipClickListener {
    fun onChipClick(module: SelectableModule)
}
