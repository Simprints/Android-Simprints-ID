package com.simprints.infra.sync.config.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.sync.config.usecase.HandleProjectStateUseCase
import com.simprints.infra.sync.config.usecase.RescheduleWorkersIfConfigChangedUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
internal class ProjectConfigDownSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val authStore: AuthStore,
    private val configManager: ConfigManager,
    private val handleProjectState: HandleProjectStateUseCase,
    private val rescheduleWorkersIfConfigChanged: RescheduleWorkersIfConfigChangedUseCase,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {
    override val tag = "ProjectConfigDownSync"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        crashlyticsLog("Started")
        showProgressNotification()
        try {
            val projectId = authStore.signedInProjectId
            val oldConfig = configManager.getProjectConfiguration()

            // if the user is not signed in, we shouldn't try again
            if (projectId.isEmpty()) {
                fail(IllegalStateException("User is not signed in"))
            } else {
                val (project, config) = configManager.refreshProject(projectId)
                handleProjectState(project.state)
                rescheduleWorkersIfConfigChanged(oldConfig, config)

                success()
            }
        } catch (t: Throwable) {
            fail(t)
        }
    }
}
