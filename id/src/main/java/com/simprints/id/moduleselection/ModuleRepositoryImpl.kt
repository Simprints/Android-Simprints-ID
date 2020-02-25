package com.simprints.id.moduleselection

import com.simprints.id.data.db.session.crashreport.CrashReportManager
import com.simprints.id.data.db.session.crashreport.CrashReportTag
import com.simprints.id.data.db.session.crashreport.CrashReportTrigger
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.moduleselection.model.Module

class ModuleRepositoryImpl(
    val preferencesManager: PreferencesManager,
    val crashReportManager: CrashReportManager
): ModuleRepository {

    override fun getModules(): List<Module> = buildModulesList()

    override fun saveModules(modules: List<Module>) {
        setSelectedModules(modules.filter { it.isSelected })
    }

    override fun getMaxNumberOfModules(): Int = preferencesManager.maxNumberOfModules

    private fun buildModulesList() = preferencesManager.moduleIdOptions.map {
        Module(it, isModuleSelected(it))
    }

    private fun isModuleSelected(moduleName: String): Boolean {
        return preferencesManager.selectedModules.contains(moduleName)
    }

    private fun setSelectedModules(selectedModules: List<Module>) {
        preferencesManager.selectedModules = selectedModules.map { it.name }.toSet()
        logMessageForCrashReport("Modules set to ${preferencesManager.selectedModules}")
        setCrashlyticsKeyForModules()
    }

    private fun setCrashlyticsKeyForModules() {
        crashReportManager.setModuleIdsCrashlyticsKey(preferencesManager.selectedModules)
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(
            CrashReportTag.SETTINGS, CrashReportTrigger.UI, message = message
        )
    }

}
