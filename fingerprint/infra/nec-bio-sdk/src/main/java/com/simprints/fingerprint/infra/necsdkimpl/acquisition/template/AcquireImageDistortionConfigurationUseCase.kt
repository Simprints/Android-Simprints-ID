package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.simprints.fingerprint.infra.imagedistortionconfig.ImageDistortionConfigRepo
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.v2.scanner.ScannerInfo
import javax.inject.Inject

@OptIn(ExperimentalStdlibApi::class)
internal class AcquireImageDistortionConfigurationUseCase @Inject constructor(
    private val fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory,
    private val scannerInfo: ScannerInfo,
    private val imageDistortionConfigRepo: ImageDistortionConfigRepo,
) {
    /**
     * Acquires the image distortion configuration.
     * - First, attempts to retrieve the configuration from local storage.
     * - If not available, acquires it from the scanner and saves it to local storage.
     *
     * Note: This method should be called after the image acquisition process
     * to ensure the scanner's serial number is available.
     */
    suspend operator fun invoke(): ByteArray {
        val scannerId = scannerInfo.scannerId
        val un20SerialNumber = scannerInfo.un20SerialNumber

        requireNotNull(scannerId) { "Scanner ID is null. Ensure the scanner info is correctly initialized." }
        requireNotNull(un20SerialNumber) { "Serial number is null. Ensure the scanner info is correctly initialized." }

        return imageDistortionConfigRepo.getConfig(scannerId)
            ?: acquireImageDistortionConfigurationFromScanner().also { imageDistortionConfiguration ->
                imageDistortionConfigRepo.saveConfig(
                    scannerId,
                    un20SerialNumber,
                    imageDistortionConfiguration,
                )
            }
    }

    private suspend fun acquireImageDistortionConfigurationFromScanner() =
        fingerprintCaptureWrapperFactory.captureWrapper.acquireImageDistortionMatrixConfiguration()
}
