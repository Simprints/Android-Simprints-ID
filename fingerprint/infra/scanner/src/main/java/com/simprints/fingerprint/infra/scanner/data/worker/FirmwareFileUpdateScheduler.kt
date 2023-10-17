package com.simprints.fingerprint.infra.scanner.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.simprints.fingerprint.infra.scanner.BuildConfig
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * This class is responsible for scheduling the worker [FirmwareFileUpdateWorker] that updates the
 * firmware version, if any updates available, on the device.
 *
 * @property context  the application context used for scheduling the worker
 * @property configManager the configuration manager for checking the version of the connected Vero scanner
 */
class FirmwareFileUpdateScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configManager: ConfigManager
) {

    suspend fun scheduleOrCancelWorkIfNecessary() {
        if (configManager.getProjectConfiguration().fingerprint?.allowedVeroGenerations?.contains(
                FingerprintConfiguration.VeroGeneration.VERO_2
            ) == true
        ) {
            scheduleWork()
        } else {
            cancelWork()
        }
    }

    private fun scheduleWork() {
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, buildWork())
    }

    private fun cancelWork() {
        WorkManager
            .getInstance(context)
            .cancelUniqueWork(WORK_NAME)
    }

    private fun buildWork(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<FirmwareFileUpdateWorker>(REPEAT_INTERVAL, REPEAT_INTERVAL_UNIT)
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
