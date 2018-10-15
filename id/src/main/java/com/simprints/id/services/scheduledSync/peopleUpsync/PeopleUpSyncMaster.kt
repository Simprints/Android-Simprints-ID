package com.simprints.id.services.scheduledSync.peopleUpsync

import androidx.work.*
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncWorker.Companion.PROJECT_ID_KEY
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncWorker.Companion.USER_ID_KEY

class PeopleUpSyncMaster(private val workManager: WorkManager) {


    fun schedule(projectId: String, userId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val data = workDataOf(
            PROJECT_ID_KEY to projectId,
            USER_ID_KEY to userId
        )

        val workRequest = OneTimeWorkRequestBuilder<PeopleUpSyncWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        workManager
            .beginUniqueWork(
                uniqueWorkNameFor(projectId, userId),
                ExistingWorkPolicy.KEEP,
                workRequest
            )
            .enqueue()
    }

    fun pause(projectId: String, userId: String) {
        workManager
            .cancelUniqueWork(uniqueWorkNameFor(projectId, userId))
    }

    fun resume(projectId: String, userId: String) =
        schedule(projectId, userId)

    private fun uniqueWorkNameFor(projectId: String, userId: String) =
        "$projectId-$userId"

}
