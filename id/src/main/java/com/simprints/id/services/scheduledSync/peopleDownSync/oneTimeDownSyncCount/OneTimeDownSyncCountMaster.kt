package com.simprints.id.services.scheduledSync.peopleDownSync.oneTimeDownSyncCount

import androidx.work.*

class OneTimeDownSyncCountMaster(
    private val getWorkManager: () -> WorkManager = WorkManager::getInstance
) {
    fun schedule(projectId: String, userId: String) {
        getWorkManager().beginUniqueWork(
            getUniqueOneTimeDownSyncCountWorkName(projectId, userId),
            ExistingWorkPolicy.REPLACE,
            buildWorkRequest(projectId, userId)
        )
            .enqueue()
    }

    private fun buildWorkRequest(projectId: String, userId: String) =
        OneTimeWorkRequestBuilder<OneTimeDownSyncCountWorker>()
            .setConstraints(buildConstraints())
            .build()

    private fun buildConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun getUniqueOneTimeDownSyncCountWorkName(projectId: String, userId: String) =
        "$projectId-$userId-$ONE_TIME_DOWN_SYNC_WORK_NAME_SUFFIX"

    companion object {
        const val ONE_TIME_DOWN_SYNC_WORK_NAME_SUFFIX = "down-sync"
    }
}
