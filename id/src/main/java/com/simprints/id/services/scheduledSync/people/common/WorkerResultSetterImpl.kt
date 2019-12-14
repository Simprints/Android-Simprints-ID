package com.simprints.id.services.scheduledSync.people.master

import androidx.work.Data
import androidx.work.ListenableWorker.Result

class WorkerResultSetterImpl : WorkerResultSetter {

    override fun success(outputData: Data?) =
        outputData?.let {
            Result.success(it)
        } ?: Result.success()

    override fun failure(outputData: Data?) =
        outputData?.let {
            Result.failure(it)
        } ?: Result.failure()


    override fun retry() =
        Result.retry()
}
