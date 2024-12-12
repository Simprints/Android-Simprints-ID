package com.simprints.fingerprint.infra.scanner.capture

import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireUnprocessedImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.RawUnprocessedImage
import com.simprints.fingerprint.infra.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.NoImageDistortionConfigurationMatrixException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageFormatData
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import com.simprints.fingerprint.infra.scanner.v2.tools.runWithErrorWrapping

internal class FingerprintCaptureWrapperV2(
    private val scannerV2: Scanner,
    private val tracker: FingerprintScanningStatusTracker,
) : FingerprintCaptureWrapper {
    override suspend fun acquireImageDistortionMatrixConfiguration(): ByteArray = runWithErrorWrapping {
        scannerV2.acquireImageDistortionConfigurationMatrix()
            ?: throw NoImageDistortionConfigurationMatrixException("Failed to acquire image distortion configuration matrix")
    }

    override suspend fun acquireFingerprintImage(): AcquireFingerprintImageResponse = runWithErrorWrapping {
        scannerV2.acquireImage(IMAGE_FORMAT)?.image?.let { imageBytes ->
            AcquireFingerprintImageResponse(imageBytes)
        } ?: throw NoFingerDetectedException("Failed to acquire image")
    }

    override suspend fun acquireUnprocessedImage(captureDpi: Dpi?): AcquireUnprocessedImageResponse = runWithErrorWrapping {
        require(captureDpi != null && (captureDpi.value in MIN_CAPTURE_DPI..MAX_CAPTURE_DPI)) {
            "Capture DPI must be between $MIN_CAPTURE_DPI and $MAX_CAPTURE_DPI"
        }
        // Capture fingerprint and ensure it's OK
        scannerV2.captureFingerprint().ensureCaptureResultOkOrError()
        tracker.completeScan()
        // Transfer the unprocessed image from the scanner
        scannerV2.acquireUnprocessedImage(IMAGE_FORMAT)?.image?.let { imageData ->
            AcquireUnprocessedImageResponse(
                RawUnprocessedImage(
                    imageData,
                ),
            )
        } ?: throw NoFingerDetectedException("Failed to acquire unprocessed image data")
    }

    override suspend fun acquireFingerprintTemplate(
        captureDpi: Dpi?,
        timeOutMs: Int,
        qualityThreshold: Int,
        allowLowQualityExtraction: Boolean,
    ): AcquireFingerprintTemplateResponse = runWithErrorWrapping {
        require(captureDpi != null && (captureDpi.value in MIN_CAPTURE_DPI..MAX_CAPTURE_DPI)) {
            "Capture DPI must be between $MIN_CAPTURE_DPI and $MAX_CAPTURE_DPI"
        }
        scannerV2.captureFingerprint(captureDpi).ensureCaptureResultOkOrError()
        tracker.completeScan()
        val qualityScore = scannerV2.getImageQualityScore()
            ?: throw NoFingerDetectedException("Failed to acquire image quality score")
        validateMinimumFingerImageQuality(qualityScore)

        // If the quality score is below the threshold and we don't allow low quality extraction, return an empty template
        if (qualityScore < qualityThreshold && !allowLowQualityExtraction) {
            AcquireFingerprintTemplateResponse(
                ByteArray(0),
                templateFormat,
                qualityScore,
            )
        } else {
            acquireTemplateAndAssembleResponse(qualityScore)
                ?: throw NoFingerDetectedException("Failed to acquire template")
        }
    }

    private fun CaptureFingerprintResult.ensureCaptureResultOkOrError() = when (this) {
        CaptureFingerprintResult.OK -> { // Do nothing
        }

        CaptureFingerprintResult.FINGERPRINT_NOT_FOUND -> throw NoFingerDetectedException("Fingerprint not found")
        CaptureFingerprintResult.DPI_UNSUPPORTED -> throw UnexpectedScannerException("Capture fingerprint DPI unsupported")
        CaptureFingerprintResult.UNKNOWN_ERROR -> throw UnknownScannerIssueException("Unknown error when capturing fingerprint")
    }

    private fun validateMinimumFingerImageQuality(qualityScore: Int) {
        if (qualityScore <= NO_FINGER_IMAGE_QUALITY_THRESHOLD) {
            throw NoFingerDetectedException("Image quality score below detection threshold")
        }
    }

    private suspend fun acquireTemplateAndAssembleResponse(imageQuality: Int) = scannerV2.acquireTemplate()?.template?.let { templateData ->
        AcquireFingerprintTemplateResponse(
            templateData,
            templateFormat,
            imageQuality,
        )
    }

    companion object {
        private const val NO_FINGER_IMAGE_QUALITY_THRESHOLD =
            10 // The image quality at which we decide a fingerprint wasn't detected
        private val IMAGE_FORMAT = ImageFormatData.WSQ(15)
        private const val MIN_CAPTURE_DPI = 500
        private const val MAX_CAPTURE_DPI = 1700
    }
}
