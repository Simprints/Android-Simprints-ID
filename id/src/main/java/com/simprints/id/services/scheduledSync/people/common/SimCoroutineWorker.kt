package com.simprints.id.services.scheduledSync.people.common

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException

abstract class SimCoroutineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    abstract val tag: String
    var resultSetter: WorkerResultSetter = WorkerResultSetterImpl()
    abstract var crashReportManager: CrashReportManager

    protected inline fun <reified T> getComponent(block: (component: AppComponent) -> Unit) {
        val context = applicationContext
        if (context is Application) {
            block(context.component)
        } else throw WorkerInjectionFailedException.forWorker<T>()
    }

    protected fun retry(t: Throwable? = null, message: String = t?.message ?: ""): Result {
        t?.printStackTrace()
        crashlyticsLog("[Retry] $message")
        return resultSetter.retry()
    }

    protected fun fail(t: Throwable,
                       message: String? = t.message ?: "",
                       outputData: Data? = null): Result {

        t.printStackTrace()
        crashlyticsLog("[Failed] $message")

        crashReportManager.logExceptionOrSafeException(t)
        return resultSetter.failure(outputData)
    }

    protected fun success(outputData: Data? = null,
                          message: String = ""): Result {
        crashlyticsLog("[Success] $message")
        return resultSetter.success(outputData)
    }

    protected fun crashlyticsLog(message: String) {
        crashReportManager.logMessageForCrashReport(
            CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = "$tag - $message")
    }
}
