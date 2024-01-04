package com.simprints.infra.config.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.infra.logging.Simber
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.sync.ConfigManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
internal class ConfigurationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val authStore: AuthStore,
    private val configManager: ConfigManager
) : CoroutineWorker(context, params) {

    private val tag = "ConfigurationWorker"

    override suspend fun doWork(): Result = try {
        val projectId = authStore.signedInProjectId

        // if the user is not signed in, we shouldn't try again
        if (projectId.isEmpty()) {
            Result.failure()
        } else {
            configManager.refreshProject(projectId)
            configManager.refreshProjectConfiguration(projectId)
            Simber.tag(tag).i("Successfully refresh the project configuration")
            Result.success()
        }
    } catch (e: Exception) {
        Simber.tag(tag).i("Failed to refresh the project configuration")
        Result.failure()
    }
}
