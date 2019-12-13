package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber

abstract class SimCoroutineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var resultSetter: WorkerResultSetter = WorkerResultSetterImpl()
    abstract var crashReportManager: CrashReportManager

    protected inline fun <reified T> getComponent(block: (component: AppComponent) -> Unit) {
        val context = applicationContext
        if (context is Application) {
            block(context.component)
        } else throw WorkerInjectionFailedException.forWorker<T>()
    }

    protected inline fun <reified T> logFailure(message: Any, t: Throwable) {
        crashReportManager.logExceptionOrSafeException(t)
        showToastForDebug<T>(message, Result.failure())
    }

    protected inline fun <reified T> logSuccess(message: Any) {
        logMessageForCrashReport<T>(message)
        showToastForDebug<T>(message, Result.success())
        Timber.d("$message")
    }

    protected inline fun <reified T> showToastForDebug(input: Any, result: Result) {
        if (BuildConfig.DEBUG) {
            val message = "${T::class.java.canonicalName} - Input: ($input) Result: $result"

            applicationContext.runOnUiThread {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                Timber.d(message)
            }
        }
    }

    protected inline fun <reified T> logMessageForCrashReport(message: Any) {
        crashReportManager.logMessageForCrashReport(
            CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = "${T::class.java.canonicalName} - $message")
    }
}
