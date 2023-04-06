package com.simprints.id.services.config

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters

@Deprecated(message = "We need to keep this worker until all the devices have updated to" +
    "the version 2022.4.0, otherwise the app will crash as the work manager will not find the class" +
    "to run the previous work to fetch the configuration.")
class RemoteConfigWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        internal const val WORK_NAME = "remote-config-work"
    }

    private val workManager = WorkManager.getInstance(context)

    override suspend fun doWork(): Result {
        workManager.cancelUniqueWork(WORK_NAME)
        return Result.success()
    }

}
