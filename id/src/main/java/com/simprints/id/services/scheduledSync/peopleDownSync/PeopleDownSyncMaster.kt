package com.simprints.id.services.scheduledSync.peopleDownSync

import androidx.work.*

class PeopleDownSyncMaster(
    private val getWorkManager: () -> WorkManager = WorkManager::getInstance
) {
    fun schedule(projectId: String) {
        getWorkManager().beginUniqueWork(
            getUniqueDownSyncWorkName(projectId),
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

    private fun getUniqueDownSyncWorkName(projectId: String) =
        "$projectId-$DOWN_SYNC_WORK_NAME_SUFFIX"

    companion object {
        const val DOWN_SYNC_WORK_NAME_SUFFIX = "down-sync"
    }
}
