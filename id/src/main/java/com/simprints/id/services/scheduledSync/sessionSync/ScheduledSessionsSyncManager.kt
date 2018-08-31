package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.*
import java.util.concurrent.TimeUnit

class ScheduledSessionsSyncManager {

    fun scheduleSyncIfNecessary() = createAndEnqueueRequest()

    private fun createAndEnqueueRequest(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<ScheduledSessionsSync>(SYNC_REPEAT_INTERVAL, SYNC_REPEAT_UNIT)
            .setConstraints(getConstraints())
            .addTag(WORKER_TAG)
            .build().also {
                WorkManager.getInstance().enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, it)
            }

    private fun getConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    companion object {
        private const val SYNC_REPEAT_INTERVAL = 6L
        private val SYNC_REPEAT_UNIT = TimeUnit.HOURS
        private const val WORKER_TAG = "SYNC_SESSIONS_WORKER"
    }
}
