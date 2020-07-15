package com.simprints.id.services.securitystate

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class SecurityStateSchedulerImpl(context: Context) : SecurityStateScheduler {

    private val workManager = WorkManager.getInstance(context)

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
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        return PeriodicWorkRequestBuilder<SecurityStateWorker>(REPEAT_INTERVAL, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            ).build()
    }

    private companion object {
        const val WORK_NAME = "security-status-check-work"
        const val REPEAT_INTERVAL = 15L
    }

}
