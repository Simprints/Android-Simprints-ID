package com.simprints.infra.config.worker

import android.content.Context
import androidx.work.*
import com.simprints.infra.config.BuildConfig
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class ConfigurationSchedulerImpl @Inject constructor(context: Context) :
    ConfigurationScheduler {

    companion object {
        const val WORK_NAME = "project-configuration-work"
        private const val SYNC_REPEAT_INTERVAL = BuildConfig.SYNC_PERIODIC_WORKER_INTERVAL_MINUTES
        private val SYNC_REPEAT_UNIT = TimeUnit.MINUTES
    }

    private val workManager = WorkManager.getInstance(context)

    override fun scheduleSync() {
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            buildPeriodicRequest()
        )
    }

    override fun cancelScheduledSync() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private fun buildPeriodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<ConfigurationWorker>(SYNC_REPEAT_INTERVAL, SYNC_REPEAT_UNIT)
            .setConstraints(workerConstraints())
            .build()

    private fun workerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

}
