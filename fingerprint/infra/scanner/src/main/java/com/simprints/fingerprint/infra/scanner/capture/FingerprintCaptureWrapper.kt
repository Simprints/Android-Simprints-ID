package com.simprints.fingerprint.infra.scanner.capture

import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireUnprocessedImageResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi

interface FingerprintCaptureWrapper {
    suspend fun acquireFingerprintImage(): AcquireFingerprintImageResponse

    suspend fun acquireUnprocessedImage(captureDpi: Dpi?): AcquireUnprocessedImageResponse

    suspend fun acquireFingerprintTemplate(
        captureDpi: Dpi?,
        timeOutMs: Int,
        qualityThreshold: Int,
        allowLowQualityExtraction: Boolean,
    ): AcquireFingerprintTemplateResponse

    val templateFormat: String
        get() = "ISO_19794_2" // This is the only template format Veros support

    suspend fun acquireImageDistortionMatrixConfiguration(): ByteArray
}
