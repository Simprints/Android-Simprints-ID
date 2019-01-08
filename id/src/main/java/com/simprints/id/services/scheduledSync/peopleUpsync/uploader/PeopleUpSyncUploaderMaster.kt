package com.simprints.id.services.scheduledSync.peopleUpsync.uploader

import androidx.work.*
import java.util.concurrent.TimeUnit

// TODO: uncomment userId when multitenancy is properly implemented

class PeopleUpSyncUploaderMaster(
    private val getWorkManager: () -> WorkManager = WorkManager::getInstance
) {

    fun schedule(projectId: String/*, userId: String*/) {
        getWorkManager()
            .beginUniqueWork(
                uniqueWorkNameFor(projectId/*, userId*/),
                ExistingWorkPolicy.KEEP,
                buildWorkRequest(projectId/*, userId*/)
            )
            .enqueue()
    }

    private fun buildWorkRequest(projectId: String/*, userId: String*/) =
        OneTimeWorkRequestBuilder<PeopleUpSyncUploaderWorker>()
            .setConstraints(buildConstraints())
            .setInputData(buildWorkData(projectId/*, userId*/))
            // Retry in case of a transient sync failure
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                PEOPLE_UP_SYNC_UPLOAD_BACKOFF_DELAY,
                PEOPLE_UP_SYNC_UPLOAD_BACKOFF_UNIT)
            .build()

    private fun buildConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun buildWorkData(projectId: String/*, userId: String*/) =
        workDataOf(
            PeopleUpSyncUploaderWorker.PROJECT_ID_KEY to projectId/*,
            PeopleUpSyncUploaderWorker.USER_ID_KEY to userId*/
        )

    fun cancel(projectId: String/*, userId: String*/) {
        getWorkManager().cancelUniqueWork(uniqueWorkNameFor(projectId/*, userId*/))
    }

    private fun uniqueWorkNameFor(projectId: String/*, userId: String*/) =
        projectId /*"$projectId-$userId"*/

    companion object {
        private const val PEOPLE_UP_SYNC_UPLOAD_BACKOFF_DELAY = 30L
        private val PEOPLE_UP_SYNC_UPLOAD_BACKOFF_UNIT = TimeUnit.SECONDS
    }
}
