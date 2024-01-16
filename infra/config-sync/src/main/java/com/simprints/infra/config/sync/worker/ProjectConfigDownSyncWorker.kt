package com.simprints.infra.config.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.logging.Simber
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
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {

    override val tag = "ProjectConfigWorker"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        showProgressNotification()

        try {
            val projectId = authStore.signedInProjectId

            // if the user is not signed in, we shouldn't try again
            if (projectId.isEmpty()) {
                fail(IllegalStateException("User is not signed in"))
            } else {
                configRepository.refreshProject(projectId)

                crashlyticsLog("Successfully refresh the project configuration")
                success()
            }
        } catch (t: Throwable) {
            fail(t)
        }
    }
}
