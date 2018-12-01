package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers

import android.widget.Toast
import androidx.work.Worker
import com.simprints.id.BuildConfig
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber

/**
 * Tris - Worker to fetch counter for (p, u, m) using CountTask.
 * Invocated by CountWorker
 */
class InputMergeWorker : Worker() {

    override fun doWork(): Result {
        Timber.d("$inputData")
        outputData = inputData

        return Result.SUCCESS.also {
            if (BuildConfig.DEBUG) {
                applicationContext.runOnUiThread {
                    val message = "WM - InputMergeWorker: $it $inputData"
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    Timber.d(message)
                }
            }
        }
    }
}
