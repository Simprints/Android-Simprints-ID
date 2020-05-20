package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion
import com.simprints.fingerprint.scanner.domain.versions.ScannerApiVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.*
import com.simprints.fingerprint.scanner.exceptions.unexpected.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprintscanner.v1.SCANNER_ERROR
import com.simprints.fingerprintscanner.v1.SCANNER_ERROR.*
import com.simprints.fingerprintscanner.v1.ScannerCallback
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import com.simprints.fingerprintscanner.v1.ButtonListener as ScannerTriggerListenerV1
import com.simprints.fingerprintscanner.v1.Scanner as ScannerV1

class ScannerWrapperV1(private val scannerV1: ScannerV1) : ScannerWrapper {

    override fun versionInformation(): ScannerVersion =
        ScannerVersion(
            ScannerGeneration.VERO_1,
            ScannerFirmwareVersions(
                cypress = ChipFirmwareVersion.UNKNOWN,
                stm = ChipFirmwareVersion(scannerV1.ucVersion.toInt(), 0),
                un20 = ChipFirmwareVersion(scannerV1.unVersion.toInt(), 0)
            ),
            ScannerApiVersions.UNKNOWN
        )

    override fun connect(): Completable = Completable.create { result ->
        scannerV1.connect(ScannerCallbackWrapper({
            result.onComplete()
        }, { scannerError ->
            scannerError?.let {
                val issue = when (scannerError) {
                    BLUETOOTH_DISABLED -> BluetoothNotEnabledException()
                    BLUETOOTH_NOT_SUPPORTED -> BluetoothNotSupportedException()
                    SCANNER_UNBONDED -> ScannerNotPairedException()
                    BUSY, IO_ERROR -> ScannerDisconnectedException()
                    else -> UnknownScannerIssueException.forScannerError(scannerError)
                }
                result.onError(issue)
            } ?: result.onComplete()
        }))
    }

    override fun setup(): Completable = Completable.complete()

    override fun disconnect(): Completable = Completable.create { result ->
        scannerV1.disconnect(ScannerCallbackWrapper({
            result.onComplete()
        }, {
            result.onComplete()
        }))
    }

    override fun sensorWakeUp(): Completable = Completable.create { result ->
        scannerV1.un20Wakeup(ScannerCallbackWrapper({
            result.onComplete()
        }, { scannerError ->

            scannerError?.let {
                val issue = when (scannerError) {
                    UN20_LOW_VOLTAGE -> {
                        ScannerLowBatteryException()
                    }
                    else -> UnknownScannerIssueException.forScannerError(scannerError)
                }
                result.onError(issue)
            } ?: result.onComplete()
        }
        ))
    }

    override fun sensorShutDown(): Completable = Completable.create { result ->
        scannerV1.un20Shutdown(ScannerCallbackWrapper({
            result.onComplete()
        }, {
            result.onError(UnknownScannerIssueException.forScannerError(it))
        }))
    }

    override fun captureFingerprint(captureFingerprintStrategy: CaptureFingerprintStrategy, timeOutMs: Int, qualityThreshold: Int): Single<CaptureFingerprintResponse> =
        Single.create<CaptureFingerprintResponse> { emitter ->
            scannerV1.startContinuousCapture(qualityThreshold, timeOutMs.toLong(), continuousCaptureCallback(qualityThreshold, emitter))
        }.doOnDispose {
            scannerV1.stopContinuousCapture()
        }

    private fun continuousCaptureCallback(qualityThreshold: Int, emitter: SingleEmitter<CaptureFingerprintResponse>) =
        ScannerCallbackWrapper(
            success = {
                emitter.onSuccess(CaptureFingerprintResponse(scannerV1.template!!, scannerV1.imageQuality))
            },
            failure = {
                if (it == TIMEOUT)
                    scannerV1.forceCapture(qualityThreshold, forceCaptureCallback(emitter))
                else handleFingerprintCaptureError(it, emitter)
            }
        )

    private fun forceCaptureCallback(emitter: SingleEmitter<CaptureFingerprintResponse>) =
        ScannerCallbackWrapper(
            success = {
                emitter.onSuccess(CaptureFingerprintResponse(scannerV1.template!!, scannerV1.imageQuality))
            },
            failure = {
                handleFingerprintCaptureError(it, emitter)
            }
        )

    private fun handleFingerprintCaptureError(error: SCANNER_ERROR?, emitter: SingleEmitter<CaptureFingerprintResponse>) {
        when (error) {
            UN20_SDK_ERROR -> emitter.onError(NoFingerDetectedException()) // If no finger is detected on the sensor
            INVALID_STATE, SCANNER_UNREACHABLE, UN20_INVALID_STATE, OUTDATED_SCANNER_INFO, IO_ERROR -> emitter.onError(ScannerDisconnectedException())
            BUSY, INTERRUPTED, TIMEOUT -> emitter.onError(ScannerOperationInterruptedException())
            else -> emitter.onError(UnexpectedScannerException.forScannerError(error, "ScannerWrapperV1"))
        }
    }

    override fun acquireImage(saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy): Single<AcquireImageResponse> =
        Single.error(UnavailableVero2FeatureException(UnavailableVero2Feature.IMAGE_ACQUISITION))

    override fun setUiIdle(): Completable = Completable.create { result ->
        scannerV1.resetUI(ScannerCallbackWrapper({
            result.onComplete()
        }, {
            result.onComplete() // This call on Vero 1 can sometimes be buggy
        }))
    }

    private val triggerListenerToV1Map = mutableMapOf<ScannerTriggerListener, ScannerTriggerListenerV1>()

    override fun registerTriggerListener(triggerListener: ScannerTriggerListener) {
        triggerListenerToV1Map[triggerListener] = ScannerTriggerListenerV1 { triggerListener.onTrigger() }.also {
            scannerV1.registerButtonListener(it)
        }
    }

    override fun unregisterTriggerListener(triggerListener: ScannerTriggerListener) {
        triggerListenerToV1Map[triggerListener]?.let {
            scannerV1.unregisterButtonListener(it)
        }
    }

    override fun performCypressOta(): Observable<CypressOtaStep> =
        Observable.error(UnavailableVero2FeatureException(UnavailableVero2Feature.OTA))

    private class ScannerCallbackWrapper(val success: () -> Unit, val failure: (scannerError: SCANNER_ERROR?) -> Unit) : ScannerCallback {
        override fun onSuccess() {
            success()
        }

        override fun onFailure(error: SCANNER_ERROR?) {
            failure(error)
        }
    }
}
