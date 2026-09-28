package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import javax.inject.Inject

internal class HandleProjectStateUseCase @Inject constructor(
    private val deviceStateDataTracker: DeviceStateDataTracker,
    private val syncOrchestrator: SyncOrchestrator,
) {
    suspend operator fun invoke(state: ProjectState) {
        if (shouldSignOut(state)) {
            syncOrchestrator.execute(OneTime.Logout.start(isProjectEnded = true))
        }
    }

    private suspend fun shouldSignOut(projectState: ProjectState): Boolean {
        val isProjectEnded = projectState == ProjectState.PROJECT_ENDED
        val isProjectEnding = projectState == ProjectState.PROJECT_ENDING

        return isProjectEnded || (isProjectEnding && !deviceStateDataTracker.hasPendingEvents())
    }
}
