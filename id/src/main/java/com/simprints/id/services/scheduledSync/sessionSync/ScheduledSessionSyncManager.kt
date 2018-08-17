package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.*
import com.simprints.id.data.prefs.PreferencesManager
import java.util.*
import java.util.concurrent.TimeUnit


class ScheduledSessionSyncManager(private val preferencesManager: PreferencesManager) {

    fun scheduleSyncIfNecessary() {
        val scheduledSyncRequest = createRequestAndSaveId()
        WorkManager.getInstance()?.enqueue(scheduledSyncRequest)
    }

    private fun createRequestAndSaveId(): PeriodicWorkRequest {
        val scheduledSyncRequest =
            PeriodicWorkRequestBuilder<ScheduledSessionsSync>(SYNC_REPEAT_INTERVAL, SYNC_REPEAT_UNIT)
                .setConstraints(getConstraints())
                .build()
        saveWorkRequestId(scheduledSyncRequest.id)
        return scheduledSyncRequest
    }

    //StopShip: check conditions
    private fun getConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    private fun saveWorkRequestId(id: UUID) {
        preferencesManager.scheduledSessionsSyncWorkRequestId = id.toString()
    }

    companion object {
        private const val SYNC_REPEAT_INTERVAL = 6L
        private val SYNC_REPEAT_UNIT = TimeUnit.HOURS
    }
}
