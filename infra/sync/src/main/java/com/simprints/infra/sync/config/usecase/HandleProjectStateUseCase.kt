package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.eventsync.EventSyncManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class HandleProjectStateUseCase @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val logoutUseCase: LogoutUseCase,
) {
    suspend operator fun invoke(state: ProjectState) {
        if (shouldSignOut(state)) {
            logoutUseCase()
        }
    }

    private suspend fun shouldSignOut(projectState: ProjectState): Boolean {
        val isProjectEnded = projectState == ProjectState.PROJECT_ENDED
        val isProjectEnding = projectState == ProjectState.PROJECT_ENDING
        val hasNoEventsToUpload = eventSyncManager.countEventsToUpload(null).first() == 0

        return isProjectEnded || (isProjectEnding && hasNoEventsToUpload)
    }
}
