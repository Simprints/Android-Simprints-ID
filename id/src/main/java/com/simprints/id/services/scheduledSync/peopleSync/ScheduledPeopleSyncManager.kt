package com.simprints.id.services.scheduledSync.peopleSync

import androidx.work.*
import com.simprints.id.data.prefs.PreferencesManager
import java.util.*
import java.util.concurrent.TimeUnit


class ScheduledPeopleSyncManager(private val preferencesManager: PreferencesManager) {

    fun scheduleSyncIfNecessary() {
        val scheduledSyncRequest = createRequestAndSaveId()
        WorkManager.getInstance()?.enqueue(scheduledSyncRequest)
    }

    private fun createRequestAndSaveId(): PeriodicWorkRequest {
        val scheduledSyncRequest =
            PeriodicWorkRequestBuilder<ScheduledPeopleSync>(SYNC_REPEAT_INTERVAL, SYNC_REPEAT_UNIT)
                .setConstraints(getConstraints())
                .build()
        saveWorkRequestId(scheduledSyncRequest.id)
        return scheduledSyncRequest
    }

    private fun getConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    private fun saveWorkRequestId(id: UUID) {
        preferencesManager.scheduledPeopleSyncWorkRequestId = id.toString()
    }

    companion object {
        private const val SYNC_REPEAT_INTERVAL = 6L
        private val SYNC_REPEAT_UNIT = TimeUnit.HOURS
    }
}
