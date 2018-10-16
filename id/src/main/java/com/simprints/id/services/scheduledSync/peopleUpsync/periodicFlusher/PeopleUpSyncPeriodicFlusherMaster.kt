package com.simprints.id.services.scheduledSync.peopleUpsync.periodicFlusher

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class PeopleUpSyncPeriodicFlusherMaster(
    private val getWorkManager: () -> WorkManager = WorkManager::getInstance
) {

    fun enablePeriodicFlusherFor(projectId: String, userId: String) {
        getWorkManager()
            .enqueueUniquePeriodicWork(
                uniqueWorkNameFor(projectId, userId),
                ExistingPeriodicWorkPolicy.KEEP,
                buildWorkRequest(projectId, userId)
            )
    }

    private fun buildWorkRequest(projectId: String, userId: String) =
        PeriodicWorkRequestBuilder<PeopleUpSyncPeriodicFlusherWorker>(1, TimeUnit.HOURS)
            .setInputData(buildWorkData(projectId, userId))
            .build()

    private fun buildWorkData(projectId: String, userId: String) =
        workDataOf(
            PeopleUpSyncPeriodicFlusherWorker.PROJECT_ID_KEY to projectId,
            PeopleUpSyncPeriodicFlusherWorker.USER_ID_KEY to userId
        )

    private fun uniqueWorkNameFor(projectId: String, userId: String) =
        "PeriodicFlusher-$projectId-$userId"

    fun disablePeriodicFlusherFor(projectId: String, userId: String) {
        getWorkManager()
            .cancelUniqueWork(uniqueWorkNameFor(projectId, userId))
    }

}
