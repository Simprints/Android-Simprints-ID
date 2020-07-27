package com.simprints.id.moduleselection

import com.simprints.id.moduleselection.model.Module

interface ModuleRepository {
    fun getModules(): List<Module>
    suspend fun saveModules(modules: List<Module>)
    fun getMaxNumberOfModules(): Int
}
