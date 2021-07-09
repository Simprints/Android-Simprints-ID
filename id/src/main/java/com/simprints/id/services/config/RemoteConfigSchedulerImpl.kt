package com.simprints.id.services.config

import android.content.Context
import androidx.work.*
import com.simprints.id.BuildConfig
import com.simprints.logging.Simber
import java.util.concurrent.TimeUnit

class RemoteConfigSchedulerImpl(context: Context) : RemoteConfigScheduler {
    companion object {
        private const val WORK_NAME = "remote-config-work"
        private const val SYNC_REPEAT_INTERVAL = BuildConfig.SYNC_PERIODIC_WORKER_INTERVAL_MINUTES
        private val SYNC_REPEAT_UNIT = TimeUnit.MINUTES
    }

    private val workManager = WorkManager.getInstance(context)

    override fun syncNow() {
        Simber.d("[REMOTE_CONFIG] One time sync starting")

        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.KEEP,
            buildOneTimeRequest()
        )
    }

    override fun scheduleSync() {
        Simber.d("[REMOTE_CONFIG] Scheduling periodic sync")

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            buildPeriodicRequest()
        )
    }

    override fun cancelScheduledSync() {
        Simber.d("[REMOTE_CONFIG] Canceling worker")

        workManager.cancelUniqueWork(WORK_NAME)
    }

    private fun buildOneTimeRequest(): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<RemoteConfigWorker>()
            .setConstraints(workerConstraints())
            .build()

    private fun buildPeriodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<RemoteConfigWorker>(SYNC_REPEAT_INTERVAL, SYNC_REPEAT_UNIT)
            .setConstraints(workerConstraints())
            .build()

    private fun workerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

}
