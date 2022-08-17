package com.simprints.id.moduleselection

import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectQuery
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.moduleselection.model.Module
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.MODULE_IDS
import com.simprints.infra.logging.Simber

class ModuleRepositoryImpl(
    val preferencesManager: IdPreferencesManager,
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
        Simber.tag(MODULE_IDS, true).i(preferencesManager.selectedModules.toString())
    }

    private fun logMessageForCrashReport(message: String) {
        Simber.tag(CrashReportTag.SETTINGS.name).i(message)
    }

}
