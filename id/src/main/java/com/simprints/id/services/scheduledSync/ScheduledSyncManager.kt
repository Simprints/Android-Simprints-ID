package com.simprints.id.services.scheduledSync

import androidx.work.*
import com.simprints.id.data.prefs.PreferencesManager
import java.util.*
import java.util.concurrent.TimeUnit


class ScheduledSyncManager(private val preferencesManager: PreferencesManager) {

    private val constraints: Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun scheduleSync() {
        val scheduledSyncRequest = createRequestAndSaveId()
        WorkManager.getInstance()?.enqueue(scheduledSyncRequest)
    }

    private fun createRequestAndSaveId(): PeriodicWorkRequest {
        val scheduledSyncRequest =
                PeriodicWorkRequestBuilder<ScheduledSync>(20, TimeUnit.SECONDS)
                    .setConstraints(constraints)
                    .build()
        saveWorkRequestId(scheduledSyncRequest.id)
        return scheduledSyncRequest
    }

    private fun saveWorkRequestId(id: UUID) {

    }
}
