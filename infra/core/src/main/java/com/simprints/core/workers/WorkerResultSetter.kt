package com.simprints.core.workers

import androidx.work.Data
import androidx.work.ListenableWorker.Result

internal class WorkerResultSetter {
    fun success(outputData: Data? = null): Result = outputData?.let { Result.success(it) } ?: Result.success()

    fun failure(outputData: Data? = null): Result = outputData?.let { Result.failure(it) } ?: Result.failure()

    fun retry() = Result.retry()
}
