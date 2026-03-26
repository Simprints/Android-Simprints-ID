package com.simprints.infra.eventsync.module

import com.simprints.core.domain.tokenization.TokenizableString
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

class ModuleSelectionRepository @Inject internal constructor(
    private val configRepository: ConfigRepository,
    private val deleteModules: DeleteModulesUseCase,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val deviceEventTracker: DeviceEventTracker,
) {
    suspend fun getModules(): List<SelectableModule> = getModuleOptions()?.let { modules ->
        val selectedModules = configRepository.getDeviceConfiguration().selectedModules.values()

        modules.map { SelectableModule(it, selectedModules.contains(it.value)) }
    } ?: emptyList()

    suspend fun forceModuleSelection(
        selectedModules: List<TokenizableString>,
        isLocalChange: Boolean,
    ) {
        val options = getModuleOptions() ?: return

        val selectedValues = selectedModules.values().toSet()
        val allModules = options.map { SelectableModule(it, selectedValues.contains(it.value)) }

        saveModules(allModules, isLocalChange)
    }

    private suspend fun getModuleOptions(): List<TokenizableString>? = configRepository
        .getProjectConfiguration()
        .synchronization.down.simprints
        ?.moduleOptions

    suspend fun saveModules(
        modules: List<SelectableModule>,
        isLocalChange: Boolean = true,
    ) {
        setSelectedModules(modules.filter { it.isSelected }, isLocalChange)
        handleUnselectedModules(modules.filter { !it.isSelected })
    }

    suspend fun getMaxNumberOfModules(): Int = configRepository
        .getProjectConfiguration()
        .synchronization.down.simprints
        ?.maxNbOfModules ?: 0

    private suspend fun setSelectedModules(
        selectedModules: List<SelectableModule>,
        isLocalChange: Boolean,
    ) {
        configRepository.updateDeviceConfiguration { configuration ->
            configuration
                .apply { this.selectedModules = selectedModules.map { module -> module.name } }
                .also {
                    logMessageForCrashReport("Modules set to ${it.selectedModules.values()}")
                    setCrashlyticsKeyForModules(it.selectedModules.values())

                    deviceEventTracker.trackDeviceConfigurationUpdatedEvent(
                        deviceConfiguration = it,
                        isLocalChange = isLocalChange,
                    )
                }
        }
    }

    private suspend fun handleUnselectedModules(unselectedModules: List<SelectableModule>) {
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
