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
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper
import com.simprints.fingerprint.infra.scanner.v2.tools.wrapErrorFromScanner
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext

internal class FingerprintCaptureWrapperV2(
    private val scannerV2: Scanner,
    private val scannerUiHelper: ScannerUiHelper,
    private val ioDispatcher: CoroutineDispatcher,
    private val scanningStatusTracker: FingerprintScanningStatusTracker,
) : FingerprintCaptureWrapper {

    override suspend fun acquireImageDistortionMatrixConfiguration(): ByteArray =
        withContext(ioDispatcher) {
            scannerV2.acquireImageDistortionConfigurationMatrix()
                .switchIfEmpty(Single.error(NoImageDistortionConfigurationMatrixException()))
                .wrapErrorsFromScanner().await()
        }

    override suspend fun acquireFingerprintImage(): AcquireFingerprintImageResponse =
        withContext(ioDispatcher) {
            scannerV2.acquireImage(IMAGE_FORMAT).map { imageBytes ->
                AcquireFingerprintImageResponse(imageBytes.image)
            }
                .switchIfEmpty(Single.error(NoFingerDetectedException("Failed to acquire image")))
                .wrapErrorsFromScanner()
                .await()
        }


    override suspend fun acquireUnprocessedImage(
        captureDpi: Dpi?,
    ): AcquireUnprocessedImageResponse =

        withContext(ioDispatcher) {
            require(captureDpi != null && (captureDpi.value in MIN_CAPTURE_DPI..MAX_CAPTURE_DPI)) {
                "Capture DPI must be between $MIN_CAPTURE_DPI and $MAX_CAPTURE_DPI"
            }
            // Capture fingerprint and ensure it's OK
            scannerV2.captureFingerprint().ensureCaptureResultOkOrError().await()
            scanningStatusTracker.notifyScanCompleted()
            // Transfer the unprocessed image from the scanner
            acquireUnprocessedImage().switchIfEmpty(Single.error(NoFingerDetectedException("Failed to acquire unprocessed image data")))
                .wrapErrorsFromScanner().await()

        }


    private fun acquireUnprocessedImage() =
        scannerV2.acquireUnprocessedImage(IMAGE_FORMAT)
            .map { imageData ->
                AcquireUnprocessedImageResponse(
                    RawUnprocessedImage(
                        imageData.image
                    )
                )
            }

    override suspend fun acquireFingerprintTemplate(
        captureDpi: Dpi?,
        timeOutMs: Int,
        qualityThreshold: Int,
        allowLowQualityExtraction: Boolean
    ): AcquireFingerprintTemplateResponse = withContext(ioDispatcher) {
        require(captureDpi != null && (captureDpi.value in MIN_CAPTURE_DPI..MAX_CAPTURE_DPI)) {
            "Capture DPI must be between $MIN_CAPTURE_DPI and $MAX_CAPTURE_DPI"
        }
        scannerV2
            .captureFingerprint(captureDpi)
            .ensureCaptureResultOkOrError()
            .andThen(Completable.fromAction {
                scanningStatusTracker.notifyScanCompleted()
            })
            .andThen(scannerV2.getImageQualityScore())
            .switchIfEmpty(Single.error(NoFingerDetectedException("Failed to acquire image quality score")))
            .setLedStateBasedOnQualityScoreOrInterpretAsNoFingerDetected(qualityThreshold)
            .flatMap { qualityScore ->
                // If the quality score is below the threshold and we don't allow low quality extraction, return an empty template
                if (qualityScore < qualityThreshold && !allowLowQualityExtraction) {
                    Single.just(AcquireFingerprintTemplateResponse(ByteArray(0), templateFormat, qualityScore))
                } else {
                    Single.just(qualityScore)
                        .acquireTemplateAndAssembleResponse()
                        .switchIfEmpty(Single.error(NoFingerDetectedException("Failed to acquire template")))
                        .ifNoFingerDetectedThenSetBadScanLedState()
                        .wrapErrorsFromScanner()
                }
            }
            .await()
    }

    private fun Single<CaptureFingerprintResult>.ensureCaptureResultOkOrError() =
        flatMapCompletable {
            when (it) {
                CaptureFingerprintResult.OK -> Completable.complete()
                CaptureFingerprintResult.FINGERPRINT_NOT_FOUND -> Completable.error(
                    NoFingerDetectedException("Fingerprint not found")
                )

                CaptureFingerprintResult.DPI_UNSUPPORTED -> Completable.error(
                    UnexpectedScannerException("Capture fingerprint DPI unsupported")
                )

                CaptureFingerprintResult.UNKNOWN_ERROR -> Completable.error(
                    UnknownScannerIssueException("Unknown error when capturing fingerprint")
                )
            }
        }

    private fun Single<Int>.setLedStateBasedOnQualityScoreOrInterpretAsNoFingerDetected(
        qualityThreshold: Int
    ) =
        flatMap { qualityScore ->
            if (qualityScore > NO_FINGER_IMAGE_QUALITY_THRESHOLD) {
                val ledState = if (qualityScore >= qualityThreshold) {
                    scannerUiHelper.goodScanLedState()
                } else {
                    scannerUiHelper.badScanLedState()
                }
                scannerV2.setSmileLedState(ledState)
                    .andThen(Single.just(qualityScore))
            } else {
                Single.error(NoFingerDetectedException("Image quality score below detection threshold"))
            }
        }

    private fun Single<Int>.acquireTemplateAndAssembleResponse() =
        flatMapMaybe { imageQuality ->
            scannerV2.acquireTemplate()
                .map { templateData ->
                    AcquireFingerprintTemplateResponse(
                        templateData.template, templateFormat, imageQuality
                    )
                }
        }


    private fun Single<AcquireFingerprintTemplateResponse>.ifNoFingerDetectedThenSetBadScanLedState() =
        onErrorResumeNext {
            if (it is NoFingerDetectedException) {
                scannerV2.setSmileLedState(scannerUiHelper.badScanLedState())
                    .andThen(Single.error(it))
            } else {
                Single.error(it)
            }
        }


    companion object {
        private const val NO_FINGER_IMAGE_QUALITY_THRESHOLD =
            10 // The image quality at which we decide a fingerprint wasn't detected
        private val IMAGE_FORMAT = ImageFormatData.WSQ(15)
        private const val MIN_CAPTURE_DPI = 500
        private const val MAX_CAPTURE_DPI = 1700
    }

    private fun <T> Single<T>.wrapErrorsFromScanner() =
        onErrorResumeNext { Single.error(wrapErrorFromScanner(it)) }

}

