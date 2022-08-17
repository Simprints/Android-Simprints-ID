package com.simprints.id.services.config

import android.content.Context
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.simprints.id.services.sync.events.common.SimCoroutineWorker

class RemoteConfigWorker(context: Context, params: WorkerParameters) :
    SimCoroutineWorker(context, params) {

    companion object {
        private const val WORK_NAME = "remote-config-work"
    }

    private val workManager = WorkManager.getInstance(context)
    override val tag: String = RemoteConfigWorker::class.java.simpleName

    override suspend fun doWork(): Result {
        workManager.cancelUniqueWork(WORK_NAME)
        return Result.success()
    }

}
