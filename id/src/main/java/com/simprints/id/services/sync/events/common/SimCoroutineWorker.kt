package com.simprints.id.services.sync.events.common

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.NetworkConnectionException
import kotlinx.coroutines.CancellationException

const val SYNC_LOG_TAG = "SYNC"

abstract class SimCoroutineWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    abstract val tag: String
    var resultSetter: WorkerResultSetter = WorkerResultSetterImpl()

    protected fun retry(t: Throwable? = null, message: String = t?.message ?: ""): Result {
        crashlyticsLog("$tag - Retry] $message")

        logExceptionIfRequired(t)
        return resultSetter.retry()
    }

    protected fun fail(
        t: Throwable,
        message: String? = t.message ?: "",
        outputData: Data? = null
    ): Result {

        crashlyticsLog("$tag - Failed] $message")
        logExceptionIfRequired(t)
        return resultSetter.failure(outputData)

    }

    protected fun success(
        outputData: Data? = null,
        message: String = ""
    ): Result {
        crashlyticsLog("$tag - Success] $message")

        return resultSetter.success(outputData)
    }

    protected fun crashlyticsLog(message: String) {
        Simber.tag(CrashReportTag.SYNC.name).i("$tag - $message")
    }

    private fun logExceptionIfRequired(t: Throwable?) {
        t?.let {
            when (t) {
                is CancellationException -> Simber.d(t)
                // Record network issues only in Analytics
                is NetworkConnectionException -> Simber.i(t)
                else -> Simber.e(t)
            }
        }
    }
}
