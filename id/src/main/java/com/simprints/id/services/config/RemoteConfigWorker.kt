package com.simprints.id.services.config

import android.content.Context
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.simprints.id.services.sync.events.common.SimCoroutineWorker

@Deprecated(message = "We need to keep this worker until all the devices have updated to" +
    "the version 2022.3.0, otherwise the app will crash as the work manager will not find the class" +
    "to run the previous work to fetch the configuration.")
class RemoteConfigWorker(context: Context, params: WorkerParameters) :
    SimCoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "remote-config-work"
    }

    private val workManager = WorkManager.getInstance(context)
    override val tag: String = RemoteConfigWorker::class.java.simpleName

    override suspend fun doWork(): Result {
        workManager.cancelUniqueWork(WORK_NAME)
        return Result.success()
    }

}
