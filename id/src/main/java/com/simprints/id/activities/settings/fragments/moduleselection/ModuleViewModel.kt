package com.simprints.id.activities.settings.fragments.moduleselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module

class ModuleViewModel(private val repository: ModuleRepository) : ViewModel() {

    fun getModules(): LiveData<List<Module>> = repository.getModules()

    fun updateModules(modules: List<Module>) {
        repository.updateModules(modules)
    }

    fun getMaxNumberOfModules(): Int = repository.getMaxNumberOfModules()

}
