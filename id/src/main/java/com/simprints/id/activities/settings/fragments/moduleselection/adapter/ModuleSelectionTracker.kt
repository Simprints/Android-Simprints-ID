package com.simprints.id.activities.settings.fragments.moduleselection.adapter

import com.simprints.id.moduleselection.model.Module

interface ModuleSelectionTracker {
    fun onSelectionStateChanged(module: Module)
}
