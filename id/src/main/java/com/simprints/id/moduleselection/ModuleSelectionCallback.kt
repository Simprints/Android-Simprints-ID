package com.simprints.id.moduleselection

interface ModuleSelectionCallback {
    fun noModulesSelected()
    fun tooManyModulesSelected()
    fun onSuccess()
}
