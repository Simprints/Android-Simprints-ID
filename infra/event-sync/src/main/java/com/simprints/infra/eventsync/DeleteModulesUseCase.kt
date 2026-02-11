package com.simprints.infra.eventsync

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import javax.inject.Inject

class DeleteModulesUseCase @Inject internal constructor(
    private val configRepository: ConfigRepository,
    private val downSyncScopeRepository: EventDownSyncScopeRepository,
) {
    suspend operator fun invoke(unselectedModules: List<String>) {
        downSyncScopeRepository.deleteOperations(
            unselectedModules,
            modes = configRepository.getProjectConfiguration().general.modalities,
        )
    }
}
