package com.simprints.fingerprint.scanner.data.worker

import android.content.Context
import androidx.work.*
import com.simprints.fingerprint.BuildConfig
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Schedules the [FirmwareFileUpdateWorker] as necessary.
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
