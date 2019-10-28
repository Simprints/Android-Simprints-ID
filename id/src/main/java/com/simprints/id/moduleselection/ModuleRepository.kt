package com.simprints.id.moduleselection

import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.moduleselection.model.Module
import javax.inject.Inject

class ModuleRepository(component: AppComponent) {

    @Inject lateinit var preferencesManager: PreferencesManager

    init {
        component.inject(this)
    }

    fun getAvailableModules(): List<Module> {
        return preferencesManager.moduleIdOptions.map { name ->
            Module(name, isSelected = false)
        }
    }

    fun getSelectedModules(): List<Module> {
        return preferencesManager.selectedModules.map { name ->
            Module(name, isSelected = true)
        }
    }

}
