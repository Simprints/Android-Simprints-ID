package com.simprints.fingerprint.scanner.data.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class FirmwareFileUpdateScheduler(val context: Context) {

    fun schedule() {
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, buildWork())
    }

    private fun buildWork(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<FirmwareFileUpdateWorker>(REPEAT_INTERVAL, REPEAT_INTERVAL_UNITS)
            .setConstraints(getConstraints())
            .build()

    private fun getConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    companion object {
        const val WORK_NAME = "firmware-file-update-work"
        const val REPEAT_INTERVAL = 15L
        val REPEAT_INTERVAL_UNITS = TimeUnit.MINUTES
    }
}
