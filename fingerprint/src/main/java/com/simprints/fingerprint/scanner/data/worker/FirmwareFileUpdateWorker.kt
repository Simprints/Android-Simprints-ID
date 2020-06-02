package com.simprints.fingerprint.scanner.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simprints.fingerprint.di.KoinInjector
import com.simprints.fingerprint.scanner.data.FirmwareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class FirmwareFileUpdateWorker(context: Context, params: WorkerParameters)
    : CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("FirmwareFileUpdateWorker started")
            KoinInjector.acquireFingerprintKoinModules()

            val firmwareRepository: FirmwareRepository by inject()

            firmwareRepository.updateStoredFirmwareFilesWithLatest()

            Timber.d("FirmwareFileUpdateWorker succeeded")
            Result.success()
        } catch (e: Throwable) {
            Timber.e(e, "FirmwareFileUpdateWorker failed")
            Result.retry()
        } finally {
            KoinInjector.releaseFingerprintKoinModules()
        }
    }
}
