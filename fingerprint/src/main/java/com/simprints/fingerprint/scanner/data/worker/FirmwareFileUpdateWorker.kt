package com.simprints.fingerprint.scanner.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.fingerprint.scanner.data.FirmwareRepository
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.isCausedFromBadNetworkConnection
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * This class is responsible for regularly downloading the latest versions of the firmware binaries,
 * if any update are available, ensuring that the latest versions are always on the phone.
 */
@HiltWorker
class FirmwareFileUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val firmwareRepository: FirmwareRepository
) : CoroutineWorker(context, params) {


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Simber.d("FirmwareFileUpdateWorker started")
            firmwareRepository.updateStoredFirmwareFilesWithLatest()
            firmwareRepository.cleanUpOldFirmwareFiles()

            Simber.d("FirmwareFileUpdateWorker succeeded")
            Result.success()
        } catch (e: Throwable) {
            when {
                e.isCausedFromBadNetworkConnection() ->
                    Simber.i(NetworkConnectionException(cause = e))
                else ->
                    Simber.e(e, "FirmwareFileUpdateWorker failed")
            }
            Result.retry()
        }
    }
}
