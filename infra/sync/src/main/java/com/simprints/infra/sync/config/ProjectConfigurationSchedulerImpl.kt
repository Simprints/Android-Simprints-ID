package com.simprints.infra.sync.config

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.simprints.infra.sync.BuildConfig
import com.simprints.infra.sync.config.worker.DeviceConfigDownSyncWorker
import com.simprints.infra.sync.config.worker.ProjectConfigDownSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class ProjectConfigurationSchedulerImpl @Inject constructor(@ApplicationContext context: Context) :
    ProjectConfigurationScheduler {

    companion object {

        internal const val PROJECT_SYNC_WORK_NAME = "project-sync-work"
        private const val PROJECT_SYNC_REPEAT_INTERVAL =
            BuildConfig.SYNC_PERIODIC_WORKER_INTERVAL_MINUTES

        internal const val DEVICE_SYNC_WORK_NAME = "device-sync-work"
        internal const val DEVICE_SYNC_WORK_NAME_ONE_TIME = "device-sync-work-one-time"
        private const val DEVICE_SYNC_REPEAT_INTERVAL =
            BuildConfig.DEVICE_PERIODIC_WORKER_INTERVAL_MINUTES

        private val SYNC_REPEAT_UNIT = TimeUnit.MINUTES
    }

    private val workManager = WorkManager.getInstance(context)

    override fun scheduleProjectSync() {
        workManager.enqueueUniquePeriodicWork(
            PROJECT_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<ProjectConfigDownSyncWorker>(
                PROJECT_SYNC_REPEAT_INTERVAL,
                SYNC_REPEAT_UNIT
            ).setConstraints(workerConstraints()).build()
        )
    }

    override fun cancelProjectSync() {
        workManager.cancelUniqueWork(PROJECT_SYNC_WORK_NAME)
    }

    override fun startDeviceSync() {
        workManager.enqueueUniqueWork(
            DEVICE_SYNC_WORK_NAME_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<DeviceConfigDownSyncWorker>()
                .setConstraints(workerConstraints())
                .build()
        )
    }

    override fun scheduleDeviceSync() {
        workManager.enqueueUniquePeriodicWork(
            DEVICE_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<DeviceConfigDownSyncWorker>(
                DEVICE_SYNC_REPEAT_INTERVAL,
                SYNC_REPEAT_UNIT
            ).setConstraints(workerConstraints()).build()
        )
    }

    override fun cancelDeviceSync() {
        workManager.cancelUniqueWork(DEVICE_SYNC_WORK_NAME)
    }

    private fun workerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

}
