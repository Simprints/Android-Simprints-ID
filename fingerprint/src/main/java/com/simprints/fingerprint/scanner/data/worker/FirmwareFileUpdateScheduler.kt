package com.simprints.fingerprint.scanner.data.worker

import android.content.Context
import androidx.work.*
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import java.util.concurrent.TimeUnit

class FirmwareFileUpdateScheduler(val context: Context, val preferencesManager: FingerprintPreferencesManager) {

    fun scheduleOrCancelWorkIfNecessary() {
        if (preferencesManager.scannerGenerations.contains(ScannerGeneration.VERO_2)) {
            scheduleWork()
        } else {
            cancelWork()
        }
    }

    private fun scheduleWork() {
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, buildWork())
    }

    private fun cancelWork() {
        WorkManager
            .getInstance(context)
            .cancelUniqueWork(WORK_NAME)
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
