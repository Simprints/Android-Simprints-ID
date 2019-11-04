package com.simprints.id.activities.settings.fragments.moduleselection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.moduleselection.model.Module
import com.simprints.id.Application as SimprintsApplication

class ModuleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ModuleRepository((application as SimprintsApplication).component)

    fun getAvailableModules(): LiveData<List<Module>> = repository.getAvailableModules()

    fun getSelectedModules(): LiveData<List<Module>> = repository.getSelectedModules()

    fun setSelectedModules(selectedModules: List<Module>) {
        repository.setSelectedModules(selectedModules)
    }

    fun getMaxSelectedModules(): LiveData<Int> = repository.getMaxSelectedModules()

}
