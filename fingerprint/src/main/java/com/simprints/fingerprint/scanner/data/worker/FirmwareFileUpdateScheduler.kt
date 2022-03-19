package com.simprints.fingerprint.scanner.data.worker

import android.content.Context
import androidx.work.*
import com.simprints.fingerprint.BuildConfig
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import java.util.concurrent.TimeUnit

/**
 * Schedules the [FirmwareFileUpdateWorker] as necessary.
 */
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
            .getInstance(context).enqueueUniqueWork(WORK_NAME,
                ExistingWorkPolicy.REPLACE,buildWork())
    }

    private fun cancelWork() {
        WorkManager
            .getInstance(context)
            .cancelUniqueWork(WORK_NAME)
    }

    private fun buildWork(): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<FirmwareFileUpdateWorker>()
            .setConstraints(getConstraints())
            .build()

    private fun getConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    companion object {
        const val WORK_NAME = "firmware-file-update-work"
        const val REPEAT_INTERVAL = BuildConfig.FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES
        val REPEAT_INTERVAL_UNIT = TimeUnit.MINUTES
    }
}
