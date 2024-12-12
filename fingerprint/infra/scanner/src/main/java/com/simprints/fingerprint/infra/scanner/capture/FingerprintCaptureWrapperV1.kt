package com.simprints.fingerprint.infra.scanner.capture

import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireUnprocessedImageResponse
import com.simprints.fingerprint.infra.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerOperationInterruptedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnavailableVero2Feature
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnavailableVero2FeatureException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR
import com.simprints.fingerprint.infra.scanner.v1.Scanner
import com.simprints.fingerprint.infra.scanner.v1.ScannerCallback
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class FingerprintCaptureWrapperV1(
    private val scannerV1: Scanner,
    private val ioDispatcher: CoroutineDispatcher,
) : FingerprintCaptureWrapper {
    override suspend fun acquireFingerprintImage(): AcquireFingerprintImageResponse =
        throw UnavailableVero2FeatureException(UnavailableVero2Feature.IMAGE_ACQUISITION)

    override suspend fun acquireUnprocessedImage(captureDpi: Dpi?): AcquireUnprocessedImageResponse =
        throw UnavailableVero2FeatureException(UnavailableVero2Feature.IMAGE_ACQUISITION)

    override suspend fun acquireImageDistortionMatrixConfiguration(): ByteArray =
        throw UnavailableVero2FeatureException(UnavailableVero2Feature.IMAGE_ACQUISITION)

    override suspend fun acquireFingerprintTemplate(
        captureDpi: Dpi?,
        timeOutMs: Int,
        qualityThreshold: Int,
        allowLowQualityExtraction: Boolean,
    ): AcquireFingerprintTemplateResponse = withContext(ioDispatcher) {
        // V1 scanner does not have a separate method to extract fingerprint template so we should
        // ignore the allowLowQualityExtraction parameter
        suspendCancellableCoroutine { cont ->
            scannerV1.startContinuousCapture(
                qualityThreshold,
                timeOutMs.toLong(),
                continuousCaptureCallback(qualityThreshold, cont),
            )

            cont.invokeOnCancellation {
                scannerV1.stopContinuousCapture()
            }
        }
    }

    private fun continuousCaptureCallback(
        qualityThreshold: Int,
        cont: Continuation<AcquireFingerprintTemplateResponse>,
    ) = ScannerCallbackWrapper(
        success = {
            cont.resume(
                AcquireFingerprintTemplateResponse(
                    scannerV1.template!!,
                    templateFormat,
                    scannerV1.imageQuality,
                ),
            )
        },
        failure = {
            if (it == SCANNER_ERROR.TIMEOUT) {
                scannerV1.forceCapture(qualityThreshold, forceCaptureCallback(cont))
            } else {
                handleFingerprintCaptureError(it, cont)
            }
        },
    )

    private fun forceCaptureCallback(cont: Continuation<AcquireFingerprintTemplateResponse>) = ScannerCallbackWrapper(
        success = {
            cont.resume(
                AcquireFingerprintTemplateResponse(
                    scannerV1.template!!,
                    templateFormat,
                    scannerV1.imageQuality,
                ),
            )
        },
        failure = {
            handleFingerprintCaptureError(it, cont)
        },
    )

    private fun handleFingerprintCaptureError(
        error: SCANNER_ERROR?,
        cont: Continuation<AcquireFingerprintTemplateResponse>,
    ) {
        when (error) {
            SCANNER_ERROR.UN20_SDK_ERROR -> cont.resumeWithException(NoFingerDetectedException("No finger detected on the sensor"))
            SCANNER_ERROR.INVALID_STATE, SCANNER_ERROR.SCANNER_UNREACHABLE, SCANNER_ERROR.UN20_INVALID_STATE, SCANNER_ERROR.OUTDATED_SCANNER_INFO, SCANNER_ERROR.IO_ERROR ->
                cont
                    .resumeWithException(
                        ScannerDisconnectedException(),
                    )

            SCANNER_ERROR.BUSY, SCANNER_ERROR.INTERRUPTED, SCANNER_ERROR.TIMEOUT -> cont.resumeWithException(
                ScannerOperationInterruptedException(),
            )

            else -> cont.resumeWithException(
                UnexpectedScannerException.forScannerError(
                    error,
                    "ScannerWrapperV1",
                ),
            )
        }
    }

    class ScannerCallbackWrapper(
        val success: () -> Unit,
        val failure: (scannerError: SCANNER_ERROR?) -> Unit,
    ) : ScannerCallback {
        override fun onSuccess() {
            success()
        }

        override fun onFailure(error: SCANNER_ERROR?) {
            failure(error)
        }
    }
}
