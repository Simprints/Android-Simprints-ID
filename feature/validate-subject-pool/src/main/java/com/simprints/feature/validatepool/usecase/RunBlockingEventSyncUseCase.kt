package com.simprints.feature.validatepool.usecase

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.extensions.await
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

internal class RunBlockingEventSyncUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val configRepository: ConfigRepository,
) {
    suspend operator fun invoke() {
        val isDownSyncAllowed = isDownSyncAllowed()

        val lastUpSyncId = syncOrchestrator.observeUpSyncState()
            .firstOrNull { !it.isUninitialized() }
            ?.syncId
        val lastDownSyncId = if (isDownSyncAllowed) {
            syncOrchestrator.observeDownSyncState()
                .firstOrNull { !it.isUninitialized() }
                ?.syncId
        } else {
            null
        }

        syncOrchestrator.execute(OneTime.UpSync.start()).await()
        if (isDownSyncAllowed) {
            syncOrchestrator.execute(OneTime.DownSync.start()).await()
        }

        syncOrchestrator.observeUpSyncState()
            .firstOrNull { it.syncId != lastUpSyncId && it.hasSyncHistory() && !it.isSyncRunning() }

        if (isDownSyncAllowed) {
            syncOrchestrator.observeDownSyncState()
                .firstOrNull { it.syncId != lastDownSyncId && it.hasSyncHistory() && !it.isSyncRunning() }
        }
    }

    private suspend fun isDownSyncAllowed(): Boolean {
        val project = configRepository.getProject()
        if (project?.state == ProjectState.PROJECT_PAUSED || project?.state == ProjectState.PROJECT_ENDING) {
            return false
        }
        val config = configRepository.getProjectConfiguration()
        return config.isSimprintsEventDownSyncAllowed() || config.isCommCareEventDownSyncAllowed()
    }
}
