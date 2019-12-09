package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber

abstract class SimCoroutineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    protected inline fun <reified T> getComponent(block: (component: AppComponent) -> Unit) {
        val context = applicationContext
        if (context is Application) {
            block(context.component)
        } else throw WorkerInjectionFailedException.forWorker<T>()
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
}
