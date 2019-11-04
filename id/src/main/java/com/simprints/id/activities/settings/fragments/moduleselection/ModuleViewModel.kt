package com.simprints.id.activities.settings.fragments.moduleselection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.Application as SimprintsApplication

class ModuleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ModuleRepository((application as SimprintsApplication).component)

    fun getModules(): LiveData<List<Module>> = repository.getModules()

    fun updateModules(modules: List<Module>) {
        repository.updateModules(modules)
    }

    fun getMaxSelectedModules(): LiveData<Int> = repository.getMaxSelectedModules()

}
