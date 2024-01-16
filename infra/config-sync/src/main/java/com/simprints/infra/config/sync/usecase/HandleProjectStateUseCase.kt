package com.simprints.infra.config.sync.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authlogic.worker.SecurityStateScheduler
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.sync.ProjectConfigurationScheduler
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class HandleProjectStateUseCase @Inject constructor(
    private val configScheduler: ProjectConfigurationScheduler,
    private val securityStateScheduler: SecurityStateScheduler,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val eventSyncManager: EventSyncManager,
    private val authManager: AuthManager
) {

    suspend operator fun invoke(projectId: String, state: ProjectState) {
        if (shouldSignOut(projectId, state)) {
            securityStateScheduler.cancelSecurityStateCheck()
            imageUpSyncScheduler.cancelImageUpSync()
            configScheduler.cancelScheduledSync()
            eventSyncManager.cancelScheduledSync()
            eventSyncManager.deleteSyncInfo()

            authManager.signOut()
        }
    }

    private suspend fun shouldSignOut(projectId: String, projectState: ProjectState): Boolean {
        val isProjectEnded = projectState == ProjectState.PROJECT_ENDED
        val isProjectEnding = projectState == ProjectState.PROJECT_ENDING
        val hasNoEventsToUpload = eventSyncManager.countEventsToUpload(projectId, null).first() == 0

        return isProjectEnded || (isProjectEnding && hasNoEventsToUpload)
    }

}
