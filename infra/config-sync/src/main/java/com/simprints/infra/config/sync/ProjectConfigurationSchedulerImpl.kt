package com.simprints.infra.config.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.simprints.infra.config.sync.worker.ProjectConfigDownSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class ProjectConfigurationSchedulerImpl @Inject constructor(@ApplicationContext context: Context) :
    ProjectConfigurationScheduler {

    companion object {

        const val WORK_NAME = "project-sync-work"
        private const val SYNC_REPEAT_INTERVAL = BuildConfig.SYNC_PERIODIC_WORKER_INTERVAL_MINUTES
        private val SYNC_REPEAT_UNIT = TimeUnit.MINUTES
    }

    private val workManager = WorkManager.getInstance(context)

    override fun scheduleSync() {
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            buildPeriodicRequest()
        )
    }

    override fun cancelScheduledSync() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private fun buildPeriodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<ProjectConfigDownSyncWorker>(SYNC_REPEAT_INTERVAL, SYNC_REPEAT_UNIT)
            .setConstraints(workerConstraints())
            .build()

    private fun workerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

}
