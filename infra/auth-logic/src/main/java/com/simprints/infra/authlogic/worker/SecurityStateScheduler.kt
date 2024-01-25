package com.simprints.infra.authlogic.worker

import android.content.Context
import androidx.work.*
import androidx.work.WorkRequest.Companion.MIN_BACKOFF_MILLIS
import com.simprints.infra.authlogic.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class SecurityStateScheduler @Inject constructor(
    @ApplicationContext context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    fun startSecurityStateCheck() {
        workManager.enqueueUniqueWork(
            WORK_NAME_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<SecurityStateWorker>()
                .setConstraints(workerConstraints())
                .build()
        )
    }

    fun scheduleSecurityStateCheck() {
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            buildWork()
        )
    }

    fun cancelSecurityStateCheck() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private fun buildWork(): PeriodicWorkRequest {

        return PeriodicWorkRequestBuilder<SecurityStateWorker>(
            REPEAT_INTERVAL, TimeUnit.MINUTES)
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
        const val WORK_NAME = "security-status-check-work-v2"
        const val WORK_NAME_ONE_TIME = "security-status-check-work-one-time-v2"
        const val REPEAT_INTERVAL = BuildConfig.SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES
    }

}