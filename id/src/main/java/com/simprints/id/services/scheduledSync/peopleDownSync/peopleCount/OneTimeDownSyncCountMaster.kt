package com.simprints.id.services.scheduledSync.peopleDownSync.peopleCount

import androidx.work.*

class OneTimeDownSyncCountMaster(
    private val getWorkManager: () -> WorkManager = WorkManager::getInstance
) {
    fun schedule(projectId: String) {
        getWorkManager().beginUniqueWork(
            getUniqueOneTimeDownSyncCountWorkName(projectId),
            ExistingWorkPolicy.REPLACE,
            buildWorkRequest()
        ).enqueue()
    }

    private fun buildWorkRequest() =
        OneTimeWorkRequestBuilder<SyncCountWorker.OneTimeWorker>()
            .setConstraints(buildConstraints())
            .build()

    private fun buildConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    fun cancelWorker(projectId: String) {
        getWorkManager().cancelUniqueWork(getUniqueOneTimeDownSyncCountWorkName(projectId))
    }

    private fun getUniqueOneTimeDownSyncCountWorkName(projectId: String) =
        "$projectId-$ONE_TIME_DOWN_SYNC_WORK_NAME_SUFFIX"

    companion object {
        const val ONE_TIME_DOWN_SYNC_WORK_NAME_SUFFIX = "down-sync-count-one-time"
    }
}
