package com.simprints.fingerprint.scanner.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.fingerprint.di.KoinInjector
import com.simprints.fingerprint.scanner.data.FirmwareRepository
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.infra.network.exceptions.isCausedFromBadNetworkConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Downloads the latest firmware binaries, ensuring that the latest versions are always on the phone.
 */
class FirmwareFileUpdateWorker(context: Context, params: WorkerParameters)
    : CoroutineWorker(context, params), KoinComponent {


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Simber.d("FirmwareFileUpdateWorker started")
            KoinInjector.acquireFingerprintKoinModules()

            val firmwareRepository: FirmwareRepository by inject()
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
        } finally {
            KoinInjector.releaseFingerprintKoinModules()
        }
    }
}
