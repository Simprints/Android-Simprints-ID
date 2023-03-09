package com.simprints.id.services.sync.events.up.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters

@Deprecated(message = "We need to keep this worker until all the devices have updated to" +
    "the version 2023.1.0, otherwise the app will crash as the work manager will not find the class" +
    "to run the previous work to fetch the configuration.")
class EventUpSyncCountWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun doWork(): Result {
        workManager.cancelAllWorkByTag("TAG_PEOPLE_SYNC_WORKER_TYPE_UP_COUNTER")
        return Result.success()
    }
}
