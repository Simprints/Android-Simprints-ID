package com.simprints.id.services.scheduledSync.peopleDownSync.periodicDownSyncCount

import androidx.work.*
import java.util.concurrent.TimeUnit

class PeriodicDownSyncCountMaster(
    private val getWorkManager: () -> WorkManager = WorkManager::getInstance
) {

    fun schedule(projectId: String) {
        getWorkManager().enqueueUniquePeriodicWork(
            getUniquePeriodicDownSyncCountWorkName(projectId),
            ExistingPeriodicWorkPolicy.KEEP,
            buildWorkRequest()
        )
    }

    private fun buildWorkRequest() =
        PeriodicWorkRequestBuilder<PeriodicDownSyncCountWorker>(DOWN_SYNC_REPEAT_INTERVAL, DOWN_SYNC_REPEAT_UNIT)
            .setConstraints(buildConstraints())
            .build()

    private fun buildConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun getUniquePeriodicDownSyncCountWorkName(projectId: String) =
        "$projectId-$PERIODIC_DOWN_SYNC_COUNT_SUFFIX"

    companion object {
        private const val DOWN_SYNC_REPEAT_INTERVAL = 6L
        private val DOWN_SYNC_REPEAT_UNIT = TimeUnit.HOURS
        private const val PERIODIC_DOWN_SYNC_COUNT_SUFFIX = "periodic-down-sync-count"
    }
}
