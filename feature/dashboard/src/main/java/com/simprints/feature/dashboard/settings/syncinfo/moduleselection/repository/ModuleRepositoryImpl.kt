package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository

import com.simprints.core.domain.tokenization.values
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.events.device.DeviceEventTracker
import com.simprints.infra.eventsync.DeleteModulesUseCase
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SETTINGS
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.MODULE_IDS
import com.simprints.infra.logging.Simber
import javax.inject.Inject

// TODO move into the event system infra module?
internal class ModuleRepositoryImpl @Inject constructor(
    private val configRepository: ConfigRepository,
    private val deleteModules: DeleteModulesUseCase,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val deviceEventTracker: DeviceEventTracker,
) : ModuleRepository {
    override suspend fun getModules(): List<Module> = configRepository
        .getProjectConfiguration()
        .synchronization.down.simprints
        ?.moduleOptions
        ?.let { modules ->
            val selectedModules = configRepository
                .getDeviceConfiguration()
                .selectedModules
                .values()

            modules.map { Module(it, selectedModules.contains(it.value)) }
        }
        ?: emptyList()

    override suspend fun saveModules(modules: List<Module>) {
        setSelectedModules(modules.filter { it.isSelected })
        handleUnselectedModules(modules.filter { !it.isSelected })
    }

    override suspend fun getMaxNumberOfModules(): Int = configRepository
        .getProjectConfiguration()
        .synchronization.down.simprints
        ?.maxNbOfModules ?: 0

    private suspend fun setSelectedModules(selectedModules: List<Module>) {
        configRepository.updateDeviceConfiguration { configuration ->
            configuration
                .apply { this.selectedModules = selectedModules.map { module -> module.name } }
                .also {
                    logMessageForCrashReport("Modules set to ${it.selectedModules.values()}")
                    setCrashlyticsKeyForModules(it.selectedModules.values())

                    deviceEventTracker.trackDeviceConfigurationUpdatedEvent(
                        deviceConfiguration = it,
                        isLocalChange = true,
                    )
                }
        }
    }

    private suspend fun handleUnselectedModules(unselectedModules: List<Module>) {
        val queries = unselectedModules.map {
            EnrolmentRecordQuery(moduleId = it.name)
        }
        enrolmentRecordRepository.delete(queries)

        // Delete operations for unselected modules to ensure full sync if they are reselected
        // in the future
        deleteModules(unselectedModules.map { it.name.value })
    }

    private fun setCrashlyticsKeyForModules(modules: List<String>) {
        Simber.setUserProperty(MODULE_IDS, modules.toString())
    }

    private fun logMessageForCrashReport(message: String) {
        Simber.i(message, tag = SETTINGS)
    }
}
