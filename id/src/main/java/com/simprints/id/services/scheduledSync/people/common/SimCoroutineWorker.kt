package com.simprints.id.services.scheduledSync.people.common

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException

abstract class SimCoroutineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    var resultSetter: WorkerResultSetter = WorkerResultSetterImpl()
    abstract var crashReportManager: CrashReportManager

    protected inline fun <reified T> getComponent(block: (component: AppComponent) -> Unit) {
        val context = applicationContext
        if (context is Application) {
            block(context.component)
        } else throw WorkerInjectionFailedException.forWorker<T>()
    }

    protected inline fun <reified T> logFailure(t: Throwable, message: String = t.message ?: "") {
        t.printStackTrace()
        crashReportManager.logMessageForCrashReport(
            CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = "${T::class.java.simpleName} - [Failed] $message")

        crashReportManager.logExceptionOrSafeException(t)
    }

    protected inline fun <reified T> logSuccess(message: Any) {
        crashReportManager.logMessageForCrashReport(
            CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = "${T::class.java.simpleName} - [Succeed] $message")
    }

    protected inline fun <reified T> crashReportLog(message: Any) {
        crashReportManager.logMessageForCrashReport(
            CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = "${T::class.java.simpleName} - $message")
    }
}
