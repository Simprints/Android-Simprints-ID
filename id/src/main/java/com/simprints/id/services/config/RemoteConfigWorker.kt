package com.simprints.id.services.config

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.logging.Simber
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteConfigWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {
    override val tag: String = RemoteConfigWorker::class.java.simpleName

    @Inject
    lateinit var dispatcherProvider: DispatcherProvider

    @Inject
    lateinit var loginInfoManager: LoginInfoManager

    @Inject
    lateinit var projectRepository: ProjectRepository

    override suspend fun doWork(): Result {
        getComponent<RemoteConfigWorker> { it.inject(this@RemoteConfigWorker) }

        return withContext(dispatcherProvider.io()) {
            try {
                crashlyticsLog("Starting")
                val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()

                // if the user is not signed in, we shouldn't try again
                if (projectId.isEmpty()) {
                    return@withContext success()
                }

                projectRepository.fetchProjectConfigurationAndSave(projectId)

                success(message = "Successfully updated configs")
            } catch (t: Throwable) {
                Simber.e(t)
                retry(message = "Failed to update configs")
            }
        }
    }
    
}
