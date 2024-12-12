package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository

internal interface ModuleRepository {
    suspend fun getModules(): List<Module>

    suspend fun saveModules(modules: List<Module>)

    suspend fun getMaxNumberOfModules(): Int
}
