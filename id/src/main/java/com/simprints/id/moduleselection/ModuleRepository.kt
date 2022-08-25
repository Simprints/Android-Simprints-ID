package com.simprints.id.moduleselection

import com.simprints.id.moduleselection.model.Module

interface ModuleRepository {
    suspend fun getModules(): List<Module>
    suspend fun saveModules(modules: List<Module>)
    suspend fun getMaxNumberOfModules(): Int
}
