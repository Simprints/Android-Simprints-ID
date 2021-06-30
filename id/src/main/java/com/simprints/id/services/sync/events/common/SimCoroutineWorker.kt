package com.simprints.id.services.sync.events.common

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.firebase.perf.metrics.Trace
import com.simprints.core.analytics.CrashReportManager
import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.analytics.CrashReportTrigger
import com.simprints.id.Application
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import com.simprints.id.tools.extensions.FirebasePerformanceTraceFactory
import com.simprints.id.tools.extensions.FirebasePerformanceTraceFactoryImpl
import com.simprints.logging.Simber
import kotlinx.coroutines.CancellationException
import java.io.IOException

const val SYNC_LOG_TAG = "SYNC"
abstract class SimCoroutineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    abstract val tag: String
    var resultSetter: WorkerResultSetter = WorkerResultSetterImpl()
    var firebasePerformanceTraceFactory: FirebasePerformanceTraceFactory = FirebasePerformanceTraceFactoryImpl()

    abstract var crashReportManager: CrashReportManager

    private var workerTrace: Trace? = null

    protected inline fun <reified T> getComponent(block: (component: AppComponent) -> Unit) {
        val context = applicationContext
        if (context is Application) {
            block(context.component)
        } else throw WorkerInjectionFailedException.forWorker<T>()
    }

    protected fun traceWorkerPerformance() {
        workerTrace = firebasePerformanceTraceFactory.newTrace("${tag}Trace")
        workerTrace?.start()
    }

    protected fun retry(t: Throwable? = null, message: String = t?.message ?: ""): Result {
        val finalMessage = "$tag - Retry] $message"
        crashlyticsLog(finalMessage)
        Simber.d(finalMessage)

        logExceptionIfRequired(t)
        workerTrace?.stop()
        return resultSetter.retry()
    }

    protected fun fail(t: Throwable,
                       message: String? = t.message ?: "",
                       outputData: Data? = null): Result {

        val finalMessage = "$tag - Failed] $message"
        crashlyticsLog(finalMessage)
        Simber.d(finalMessage)

        logExceptionIfRequired(t)
        workerTrace?.stop()
        return resultSetter.failure(outputData)
    }

    protected fun success(outputData: Data? = null,
                          message: String = ""): Result {

        val finalMessage = "$tag - Success] $message"
        crashlyticsLog(finalMessage)
        Simber.d(finalMessage)

        workerTrace?.stop()
        return resultSetter.success(outputData)
    }

    protected fun crashlyticsLog(message: String) {
        Simber.d("$tag - $message")

        crashReportManager.logMessageForCrashReport(
            CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = "$tag - $message")
    }

    private fun logExceptionIfRequired(t: Throwable?) {
        t?.let {
            Simber.d(t)
            // IOExceptions are about network issues, so they are not worth to report
            if (it !is IOException || it !is CancellationException) {
                crashReportManager.logExceptionOrSafeException(it)
            }
        }
    }
}
