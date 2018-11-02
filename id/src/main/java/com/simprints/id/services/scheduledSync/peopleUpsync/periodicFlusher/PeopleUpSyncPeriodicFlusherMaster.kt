package com.simprints.id.services.scheduledSync.peopleUpsync.periodicFlusher

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

// TODO: uncomment userId when multitenancy is properly implemented

class PeopleUpSyncPeriodicFlusherMaster(
    private val getWorkManager: () -> WorkManager = WorkManager::getInstance
) {

    fun enablePeriodicFlusherFor(projectId: String/*, userId: String*/) {
        getWorkManager()
            .enqueueUniquePeriodicWork(
                uniqueWorkNameFor(projectId/*, userId*/),
                ExistingPeriodicWorkPolicy.KEEP,
                buildWorkRequest(projectId/*, userId*/)
            )
    }

    private fun buildWorkRequest(projectId: String/*, userId: String*/) =
        PeriodicWorkRequestBuilder<PeopleUpSyncPeriodicFlusherWorker>(
            PEOPLE_UP_SYNC_FLUSHER_REPEAT_INTERVAL, PEOPLE_UP_SYNC_FLUSHER_REPEAT_UNIT)
            .setInputData(buildWorkData(projectId/*, userId*/))
            .build()

    private fun buildWorkData(projectId: String/*, userId: String*/) =
        workDataOf(
            PeopleUpSyncPeriodicFlusherWorker.PROJECT_ID_KEY to projectId/*,
            PeopleUpSyncPeriodicFlusherWorker.USER_ID_KEY to userId*/
        )

    private fun uniqueWorkNameFor(projectId: String/*, userId: String*/) =
        "PeriodicFlusher-$projectId"
    /*"PeriodicFlusher-$projectId-$userId"*/

    fun disablePeriodicFlusherFor(projectId: String/*, userId: String*/) {
        getWorkManager()
            .cancelUniqueWork(uniqueWorkNameFor(projectId/*, userId*/))
    }

    companion object {
        private const val PEOPLE_UP_SYNC_FLUSHER_REPEAT_INTERVAL = 1L
        private val PEOPLE_UP_SYNC_FLUSHER_REPEAT_UNIT = TimeUnit.HOURS
    }
}
