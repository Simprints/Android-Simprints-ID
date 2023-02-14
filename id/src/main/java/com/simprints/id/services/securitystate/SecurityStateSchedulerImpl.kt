package com.simprints.id.services.securitystate

import android.content.Context
import androidx.work.*
import androidx.work.WorkRequest.Companion.MIN_BACKOFF_MILLIS
import com.simprints.id.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// TODO move into its own module
class SecurityStateSchedulerImpl @Inject constructor(@ApplicationContext context: Context) :
    SecurityStateScheduler, com.simprints.feature.dashboard.debug.SecurityStateScheduler {

    private val workManager = WorkManager.getInstance(context)

    override fun getSecurityStateCheck() {
        workManager.enqueueUniqueWork(
            WORK_NAME_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<SecurityStateWorker>()
                .setConstraints(workerConstraints())
                .build()
        )
    }

    override fun scheduleSecurityStateCheck() {
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            buildWork()
        )
    }

    override fun cancelSecurityStateCheck() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private fun buildWork(): PeriodicWorkRequest {

        return PeriodicWorkRequestBuilder<SecurityStateWorker>(REPEAT_INTERVAL, TimeUnit.MINUTES)
            .setConstraints(workerConstraints())
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            ).build()
    }

    private fun workerConstraints(): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private companion object {
        const val WORK_NAME = "security-status-check-work"
        const val WORK_NAME_ONE_TIME = "security-status-check-work-one-time"
        const val REPEAT_INTERVAL = BuildConfig.SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES
    }

}
