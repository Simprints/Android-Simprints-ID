package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.widget.Toast
import androidx.work.Worker
import com.simprints.id.BuildConfig
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber

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
