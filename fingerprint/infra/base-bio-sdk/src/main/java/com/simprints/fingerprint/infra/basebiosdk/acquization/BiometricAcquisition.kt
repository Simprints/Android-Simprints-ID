package com.simprints.fingerprint.infra.basebiosdk.acquization

import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.CaptureFingerprintStrategy
import com.simprints.fingerprint.infra.basebiosdk.acquization.domain.SaveFingerprintImagesStrategy


class BiometricAcquisition {
    /**
     * Acquire template
     *
     * @param fingerprintTemplateProvider The fingerprint template provider to use
     * @param captureFingerprintStrategy  The capture fingerprint strategy to use
     * @param timeOutMs                  The timeout in milliseconds
     * @param qualityThreshold         The quality threshold
     * @return                        The fingerprint template
     */
    suspend fun acquireTemplate(
        fingerprintTemplateProvider: FingerprintTemplateProvider,
        captureFingerprintStrategy: CaptureFingerprintStrategy?,
        timeOutMs: Int,
        qualityThreshold: Int
    ): AcquireFingerprintTemplateResponse = fingerprintTemplateProvider.captureFingerprintTemplate(
        captureFingerprintStrategy,
        timeOutMs,
        qualityThreshold
    )

    /**
     * Acquire image
     *
     * @param fingerprintImageProvider The fingerprint image provider to use
     * @param saveFingerprintImagesStrategy The save fingerprint images strategy to use
     * @return                        The fingerprint image
     */
    suspend fun acquireImage(
        fingerprintImageProvider: FingerprintImageProvider,
        saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy?
    ): AcquireFingerprintImageResponse = fingerprintImageProvider.captureFingerprintImage(
        saveFingerprintImagesStrategy
    )

}
