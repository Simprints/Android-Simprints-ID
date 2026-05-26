package com.simprints.feature.dashboard.settings.syncinfo.usecase

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.sync.SyncOrchestrator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

internal class ObserveConfigurationChangesUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val tokenizationProcessor: TokenizationProcessor,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val syncOrchestrator: SyncOrchestrator,
) {
    operator fun invoke() = combine(
        configRepository.observeIsProjectRefreshing(),
        configRepository.observeProjectConfiguration(),
        configRepository.observeDeviceConfiguration(),
        syncCompletedSignalFlow(),
    ) { isRefreshing, projectConfig, deviceConfig, _ ->
        val project = configRepository.getProject()

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
                        EnrolmentRecordQuery(projectId = project.id, moduleId = moduleName),
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

    // Force update of module list when sync completes
    private fun syncCompletedSignalFlow(): Flow<Unit> = syncOrchestrator
        .observeSyncState()
        .map { it.eventSyncState.isSyncCompleted() }
        .distinctUntilChanged()
        .filter { it }
        .map { Unit }
        .onStart { emit(Unit) }
}

internal data class ConfigurationState(
    val isRefreshing: Boolean,
    val isProjectRunning: Boolean,
    val selectedModules: List<ModuleCount>,
    val projectConfig: ProjectConfiguration,
)
