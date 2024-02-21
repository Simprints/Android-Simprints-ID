package com.simprints.infra.config.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.sync.usecase.HandleProjectStateUseCase
import com.simprints.infra.config.sync.usecase.RescheduleWorkersIfConfigChangedUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

@HiltWorker
internal class ProjectConfigDownSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val authStore: AuthStore,
    private val configRepository: ConfigRepository,
    private val handleProjectState: HandleProjectStateUseCase,
    private val rescheduleWorkersIfConfigChanged: RescheduleWorkersIfConfigChangedUseCase,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {

    override val tag = "ProjectConfigWorker"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        showProgressNotification()

        try {
            val projectId = authStore.signedInProjectId
            val oldConfig = configRepository.getProjectConfiguration()

            // if the user is not signed in, we shouldn't try again
            if (projectId.isEmpty()) {
                fail(IllegalStateException("User is not signed in"))
            } else {
                val (project, config) = configRepository.refreshProject(projectId)
                handleProjectState(project.state)
                rescheduleWorkersIfConfigChanged(oldConfig, config)

                crashlyticsLog("Successfully refresh the project configuration")
                success()
            }
        } catch (t: Throwable) {
            fail(t)
        }
    }

}

