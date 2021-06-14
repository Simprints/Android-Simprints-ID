package com.simprints.id.moduleselection

import com.simprints.core.analytics.CrashReportManager
import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.analytics.CrashReportTrigger
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.moduleselection.model.Module

class ModuleRepositoryImpl(
    val preferencesManager: IdPreferencesManager,
    val crashReportManager: CrashReportManager,
    private val subjectRepository: SubjectRepository
): ModuleRepository {

    override fun getModules(): List<Module> = buildModulesList()

    override suspend fun saveModules(modules: List<Module>) {
        setSelectedModules(modules.filter { it.isSelected })
        handleUnselectedModules(modules.filter { !it.isSelected })
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

    private suspend fun handleUnselectedModules(unselectedModules: List<Module>) {
        val queries = unselectedModules.map {
            SubjectQuery(moduleId = it.name)
        }
        subjectRepository.delete(queries)
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
