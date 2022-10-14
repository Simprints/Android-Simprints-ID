package com.simprints.infra.config.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.infra.config.domain.ConfigService
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class ConfigurationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val loginManager: LoginManager,
    private val configService: ConfigService,
) : CoroutineWorker(context, params) {

    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    private val tag = ConfigurationWorker::class.java.name

    override suspend fun doWork(): Result =
        withContext(dispatcher) {
            try {
                val projectId = loginManager.getSignedInProjectIdOrEmpty()

                // if the user is not signed in, we shouldn't try again
                if (projectId.isEmpty()) {
                    return@withContext Result.failure()
                }

                configService.refreshConfiguration(projectId)
                Simber.tag(tag).i("Successfully refresh the project configuration")
                Result.success()
            } catch (e: Exception) {
                Simber.tag(tag).i("Failed to refresh the project configuration")
                Result.failure()
            }
        }

    // TODO remove when using hilt
    @AssistedFactory
    interface Factory {
        fun create(appContext: Context, params: WorkerParameters): ConfigurationWorker
    }
}
