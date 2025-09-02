package com.simprints.fingerprint.capture.usecase

import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.fingerprint.capture.extensions.deduceFileExtension
import com.simprints.fingerprint.capture.extensions.toInt
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.infra.scanner.v2.scanner.ScannerInfo
import com.simprints.infra.config.store.models.Vero2Configuration
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.model.SecuredImageRef
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FACE_CAPTURE
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class SaveFingerprintSampleUseCase @Inject constructor(
    private val coreImageRepository: ImageRepository,
    private val coreEventRepository: SessionEventRepository,
    private val scannerInfo: ScannerInfo,
) {
    suspend operator fun invoke(
        vero2Configuration: Vero2Configuration,
        finger: SampleIdentifier,
        captureEventId: String?,
        collectedFinger: CaptureState.ScanProcess.Collected,
    ) = if (collectedFinger.scanResult.image != null && captureEventId != null) {
        saveImage(
            imageBytes = collectedFinger.scanResult.image,
            captureEventId = captureEventId,
            fileExtension = vero2Configuration.imageSavingStrategy.deduceFileExtension(),
            finger = finger,
            dpi = vero2Configuration.captureStrategy.toInt(),
            scannerId = scannerInfo.scannerId,
            un20SerialNumber = scannerInfo.un20SerialNumber,
        )
    } else if (collectedFinger.scanResult.image != null && captureEventId == null) {
        Simber.i("Could not save fingerprint image because of null capture ID", tag = FACE_CAPTURE)
        null
    } else {
        null
    }

    private suspend fun saveImage(
        imageBytes: ByteArray,
        captureEventId: String,
        fileExtension: String,
        finger: SampleIdentifier,
        dpi: Int,
        scannerId: String?,
        un20SerialNumber: String?,
    ): SecuredImageRef? {
        val currentSession = try {
            coreEventRepository.getCurrentSessionScope()
        } catch (_: Throwable) {
            return null
        }

        return coreImageRepository.storeSample(
            projectId = currentSession.projectId,
            sessionId = currentSession.id,
            modality = Modality.FINGERPRINT,
            sampleId = captureEventId,
            fileExtension = fileExtension,
            sampleBytes = imageBytes,
            optionalMetadata = mutableMapOf(
                META_KEY_FINGER_ID to finger.name,
                META_KEY_DPI to dpi.toString(),
            ).apply {
                scannerId?.let { this[META_KEY_SCANNER_ID] = it }
                un20SerialNumber?.let { this[META_KEY_UN20_SERIAL_NUMBER] = it }
            },
        )
    }

    companion object Companion {
        private const val META_KEY_DPI = "dpi"
        private const val META_KEY_FINGER_ID = "finger"
        private const val META_KEY_SCANNER_ID = "scannerID"
        private const val META_KEY_UN20_SERIAL_NUMBER = "un20SerialNumber"
    }
}
