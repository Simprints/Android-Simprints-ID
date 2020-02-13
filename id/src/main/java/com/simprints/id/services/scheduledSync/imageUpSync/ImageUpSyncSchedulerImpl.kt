package com.simprints.id.services.scheduledSync.imageUpSync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class ImageUpSyncSchedulerImpl(context: Context) : ImageUpSyncScheduler {

    private val workManager = WorkManager.getInstance(context)

    override fun scheduleImageUpSync() {
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            buildWork()
        )
    }

    override fun cancelImageUpSync() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private fun buildWork(): PeriodicWorkRequest {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        return PeriodicWorkRequestBuilder<ImageUpSyncWorker>(15, TimeUnit.MINUTES) // STOPSHIP
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            ).build()
    }

    companion object {
        private const val WORK_NAME = "image-upsync-work"
    }

}
