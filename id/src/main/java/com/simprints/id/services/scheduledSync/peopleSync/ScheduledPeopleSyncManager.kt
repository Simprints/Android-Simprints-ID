package com.simprints.id.services.scheduledSync.peopleSync

import androidx.work.*
import com.simprints.id.data.prefs.PreferencesManager
import java.util.*
import java.util.concurrent.TimeUnit

class ScheduledPeopleSyncManager(private val preferencesManager: PreferencesManager) {

    fun scheduleSyncIfNecessary() = createAndEnqueueRequest()

    private fun createAndEnqueueRequest(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<ScheduledPeopleSync>(SYNC_REPEAT_INTERVAL, SYNC_REPEAT_UNIT)
            .setConstraints(getConstraints())
            .addTag(ScheduledPeopleSyncManager.WORKER_TAG)
            .build().also {
                WorkManager.getInstance().enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, it)
            }

    private fun getConstraints() = Constraints.Builder()
        .setRequiredNetworkType(if (preferencesManager.scheduledBackgroundSyncOnlyOnWifi) NetworkType.UNMETERED else NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(preferencesManager.scheduledBackgroundSyncOnlyWhenNotLowBattery)
        .setRequiresCharging(preferencesManager.scheduledBackgroundSyncOnlyWhenCharging)
        .build()

    companion object {
        private const val SYNC_REPEAT_INTERVAL = 15L
        private val SYNC_REPEAT_UNIT = TimeUnit.MINUTES
        private const val WORKER_TAG = "SYNC_PEOPLE_WORKER"
    }
}
