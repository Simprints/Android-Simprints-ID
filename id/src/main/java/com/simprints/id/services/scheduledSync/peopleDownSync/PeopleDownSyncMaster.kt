package com.simprints.id.services.scheduledSync.peopleDownSync

import androidx.work.*


class PeopleDownSyncMaster(
    private val getWorkManager: () -> WorkManager = WorkManager::getInstance
) {
    fun schedule(projectId: String, userId: String) {
        getWorkManager().beginUniqueWork(
            getUniqueDownSyncWorkName(projectId, userId),
            ExistingWorkPolicy.KEEP,
            buildWorkRequest()
        )
            .enqueue()
    }

    private fun buildWorkRequest() =
        OneTimeWorkRequestBuilder<PeopleDownSyncWorker>()
            .setConstraints(buildConstraints())
            .build()

    private fun buildConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun getUniqueDownSyncWorkName(projectId: String, userId: String) =
        "$projectId-$userId-$DOWN_SYNC_WORK_NAME_SUFFIX"

    companion object {
        const val DOWN_SYNC_WORK_NAME_SUFFIX = "down-sync"
    }
}
