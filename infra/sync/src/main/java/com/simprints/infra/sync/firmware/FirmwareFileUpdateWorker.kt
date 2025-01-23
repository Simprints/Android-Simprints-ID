package com.simprints.infra.sync.firmware

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.simprints.core.DispatcherBG
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.fingerprint.infra.scanner.data.FirmwareRepository
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.isCausedFromBadNetworkConnection
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * This class is responsible for downloading the latest versions of the firmware binaries,
 * if any update are available, ensuring that the latest versions are always on the phone.
 */
@HiltWorker
class FirmwareFileUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val firmwareRepository: FirmwareRepository,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {
    override val tag: String = "FirmwareFileUpdateWorker"

    override suspend fun doWork(): Result = withContext(dispatcher) {
        crashlyticsLog("Started")
        try {
            firmwareRepository.updateStoredFirmwareFilesWithLatest()
            firmwareRepository.cleanUpOldFirmwareFiles()

            success()
        } catch (e: Throwable) {
            if (e.isCausedFromBadNetworkConnection()) {
                Simber.i("Failed due to network error", NetworkConnectionException(cause = e), tag = tag)
            }
            retry(e)
        }
    }
}
