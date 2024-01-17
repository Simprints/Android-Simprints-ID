package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository

import com.simprints.core.domain.tokenization.values
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.MODULE_IDS
import com.simprints.infra.logging.Simber
import javax.inject.Inject

// TODO move into the event system infra module?
internal class ModuleRepositoryImpl @Inject constructor(
    private val configRepository: ConfigRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val eventSyncManager: EventSyncManager,
) : ModuleRepository {

    override suspend fun getModules(): List<Module> =
        configRepository.getProjectConfiguration().synchronization.down.moduleOptions.map {
            Module(it, isModuleSelected(it.value))
        }

    override suspend fun saveModules(modules: List<Module>) {
        setSelectedModules(modules.filter { it.isSelected })
        handleUnselectedModules(modules.filter { !it.isSelected })
    }

    override suspend fun getMaxNumberOfModules(): Int =
        configRepository.getProjectConfiguration().synchronization.down.maxNbOfModules

    private suspend fun isModuleSelected(moduleName: String): Boolean {
        return configRepository.getDeviceConfiguration().selectedModules.values()
            .contains(moduleName)
    }

    private suspend fun setSelectedModules(selectedModules: List<Module>) {
        configRepository.updateDeviceConfiguration {
            it.apply {
                this.selectedModules = selectedModules.map { module -> module.name }
                logMessageForCrashReport("Modules set to ${this.selectedModules.values()}")
                setCrashlyticsKeyForModules(this.selectedModules.values())
            }
        }
    }

    private suspend fun handleUnselectedModules(unselectedModules: List<Module>) {
        val queries = unselectedModules.map {
            SubjectQuery(moduleId = it.name.value)
        }
        enrolmentRecordRepository.delete(queries)

        // Delete operations for unselected modules to ensure full sync if they are reselected
        // in the future
        eventSyncManager.deleteModules(unselectedModules.map { it.name.value })
    }

    private fun setCrashlyticsKeyForModules(modules: List<String>) {
        Simber.tag(MODULE_IDS, true).i(modules.toString())
    }

    private fun logMessageForCrashReport(message: String) {
        Simber.tag(LoggingConstants.CrashReportTag.SETTINGS.name).i(message)
    }
}
