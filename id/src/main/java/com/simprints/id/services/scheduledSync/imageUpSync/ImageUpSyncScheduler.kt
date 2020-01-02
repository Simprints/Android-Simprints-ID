package com.simprints.id.services.scheduledSync.imageUpSync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class ImageUpSyncScheduler(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleWork() {
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            buildWork()
        )
    }

    fun cancelWork() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private fun buildWork(): PeriodicWorkRequest {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        return PeriodicWorkRequestBuilder<ImageUpSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
    }

    companion object {
        private const val WORK_NAME = "image-upsync-work"
    }

}
