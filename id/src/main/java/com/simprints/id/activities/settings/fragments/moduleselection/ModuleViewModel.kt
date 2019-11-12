package com.simprints.id.activities.settings.fragments.moduleselection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module

class ModuleViewModel(
    application: Application,
    private val repository: ModuleRepository
) : AndroidViewModel(application) {

    fun getModules(): LiveData<List<Module>> = repository.getModules()

    fun updateModules(modules: List<Module>) {
        repository.updateModules(modules)
    }

    fun getMaxSelectedModules(): Int = repository.getMaxSelectedModules()

}
