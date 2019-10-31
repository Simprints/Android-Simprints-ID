package com.simprints.id.moduleselection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.moduleselection.model.Module
import javax.inject.Inject

class ModuleRepository(component: AppComponent) {

    @Inject
    lateinit var preferencesManager: PreferencesManager
    @Inject
    lateinit var crashReportManager: CrashReportManager
    @Inject
    lateinit var callback: ModuleSelectionCallback

    init {
        component.inject(this)
    }

    fun getAvailableModules(): LiveData<List<Module>> = MutableLiveData<List<Module>>().apply {
        value = preferencesManager.moduleIdOptions.map { name ->
            Module(name, isSelected = false)
        }
    }

    fun getSelectedModules(): LiveData<List<Module>> = MutableLiveData<List<Module>>().apply {
        value = preferencesManager.selectedModules.map { name ->
            Module(name, isSelected = true)
        }
    }

    fun setSelectedModules(selectedModules: List<Module>) {
        if (isModuleSelectionValid(selectedModules)) {
            preferencesManager.selectedModules = selectedModules.map { it.name }.toSet()
            logMessageForCrashReport("Modules set to ${preferencesManager.selectedModules}")
            setCrashlyticsKeyForModules()
        }
    }

    private fun isModuleSelectionValid(selectedModules: List<Module>): Boolean {
        return when {
            selectedModules.isEmpty() -> {
                callback.noModulesSelected()
                false
            }

            selectedModules.size > MAX_SELECTED_MODULES -> {
                callback.tooManyModulesSelected(MAX_SELECTED_MODULES)
                false
            }

            else -> true
        }
    }

    private fun setCrashlyticsKeyForModules() {
        crashReportManager.setModuleIdsCrashlyticsKey(preferencesManager.selectedModules)
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(
            CrashReportTag.SETTINGS, CrashReportTrigger.UI, message = message
        )
    }

    private companion object {
        const val MAX_SELECTED_MODULES = 6
    }

}
