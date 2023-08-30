package com.simprints.fingerprint.infra.scanner.capture

import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintImageResponse
import com.simprints.fingerprint.infra.scanner.domain.fingerprint.AcquireFingerprintTemplateResponse
import com.simprints.fingerprint.infra.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageFormatData
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.NotConnectedException
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper
import com.simprints.infra.logging.Simber
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import java.io.IOException

class FingerprintCaptureWrapperV2(
    private val scannerV2: Scanner,
    private val scannerUiHelper: ScannerUiHelper,
    private val ioDispatcher: CoroutineDispatcher,
) : FingerprintCaptureWrapper {
    override suspend fun acquireFingerprintImage(): AcquireFingerprintImageResponse {
        return withContext(ioDispatcher) {
            scannerV2.acquireImage(IMAGE_FORMAT).map { imageBytes ->
                AcquireFingerprintImageResponse(imageBytes.image)
            }.switchIfEmpty(Single.error(NoFingerDetectedException())).wrapErrorsFromScanner()
                .await()
        }

    }

    override suspend fun acquireFingerprintTemplate(
        captureDpi: Dpi?,
        timeOutMs: Int,
        qualityThreshold: Int
    ): AcquireFingerprintTemplateResponse = withContext(ioDispatcher) {
        scannerV2
            .captureFingerprint(captureDpi!!)
            .ensureCaptureResultOkOrError()
            .andThen(scannerV2.getImageQualityScore())
            .switchIfEmpty(Single.error(NoFingerDetectedException()))
            .setLedStateBasedOnQualityScoreOrInterpretAsNoFingerDetected(qualityThreshold)
            .acquireTemplateAndAssembleResponse()
            .switchIfEmpty(Single.error(NoFingerDetectedException()))
            .ifNoFingerDetectedThenSetBadScanLedState()
            .wrapErrorsFromScanner()
            .await()
    }

    private fun Single<CaptureFingerprintResult>.ensureCaptureResultOkOrError() =
        flatMapCompletable {
            when (it) {
                CaptureFingerprintResult.OK -> Completable.complete()
                CaptureFingerprintResult.FINGERPRINT_NOT_FOUND -> Completable.error(
                    NoFingerDetectedException()
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
                Single.error(NoFingerDetectedException())
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
    }


}

fun Completable.wrapErrorsFromScanner() =
    onErrorResumeNext { Completable.error(wrapErrorFromScanner(it)) }

fun <T> Single<T>.wrapErrorsFromScanner() =
    onErrorResumeNext { Single.error(wrapErrorFromScanner(it)) }

fun wrapErrorFromScanner(e: Throwable): Throwable = when (e) {
    is NotConnectedException,
    is IOException -> { // Disconnected or timed-out communications with Scanner
        Simber.d(
            e,
            "IOException in ScannerWrapperV2, transformed to ScannerDisconnectedException"
        )
        ScannerDisconnectedException()
    }

    is IllegalStateException, // We're calling scanner methods out of order somehow
    is IllegalArgumentException -> { // We've received unexpected/invalid bytes from the scanner
        Simber.e(e)
        UnexpectedScannerException(e)
    }

    is OtaFailedException -> { // Wrap the OTA failed exception to fingerprint domain exception
        OtaFailedException("Wrapped OTA failed exception from scanner", e)
    }

    else -> { // Propagate error
        e
    }
}

fun <T> Flow<T>.mapPotentialErrorFromScanner() =
    catch { ex -> throw wrapErrorFromScanner(ex) }
