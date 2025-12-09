package com.simprints.feature.dashboard.settings.syncinfo.usecase

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal class ObserveConfigurationChangesUseCase @Inject constructor(
    private val configManager: ConfigManager,
    private val tokenizationProcessor: TokenizationProcessor,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
) {
    operator fun invoke() = combine(
        configManager.observeIsProjectRefreshing(),
        configManager.observeProjectConfiguration(),
        configManager.observeDeviceConfiguration(),
    ) { isRefreshing, projectConfig, deviceConfig ->
        val project = configManager.getProject()

        val moduleCounts = if (project != null) {
            deviceConfig.selectedModules.map { moduleName ->
                ModuleCount(
                    name = when (moduleName) {
                        is TokenizableString.Raw -> moduleName

                        is TokenizableString.Tokenized -> tokenizationProcessor.decrypt(
                            encrypted = moduleName,
                            tokenKeyType = TokenKeyType.ModuleId,
                            project,
                        )
                    }.value,
                    count = enrolmentRecordRepository.count(
                        SubjectQuery(projectId = project.id, moduleId = moduleName),
                    ),
                )
            }
        } else {
            emptyList()
        }

        ConfigurationState(
            isRefreshing = isRefreshing,
            isProjectRunning = project?.state == ProjectState.RUNNING,
            selectedModules = moduleCounts,
            projectConfig = projectConfig,
        )
    }
}

internal data class ConfigurationState(
    val isRefreshing: Boolean,
    val isProjectRunning: Boolean,
    val selectedModules: List<ModuleCount>,
    val projectConfig: ProjectConfiguration,
)
