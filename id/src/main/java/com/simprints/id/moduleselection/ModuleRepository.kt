package com.simprints.id.moduleselection

import androidx.lifecycle.LiveData
import com.simprints.id.moduleselection.model.Module

interface ModuleRepository {
    fun getModules(): LiveData<List<Module>>
    fun updateModules(modules: List<Module>)
    fun getMaxSelectedModules(): Int
}
