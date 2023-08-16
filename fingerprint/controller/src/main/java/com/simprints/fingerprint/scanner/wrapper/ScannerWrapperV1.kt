package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.data.domain.fingerprint.CaptureFingerprintStrategy
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.BLUETOOTH_DISABLED
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.BLUETOOTH_NOT_SUPPORTED
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.BUSY
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.INTERRUPTED
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.INVALID_STATE
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.IO_ERROR
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.OUTDATED_SCANNER_INFO
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.SCANNER_UNBONDED
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.SCANNER_UNREACHABLE
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.TIMEOUT
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.UN20_INVALID_STATE
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.UN20_LOW_VOLTAGE
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.UN20_SDK_ERROR
import com.simprints.fingerprint.infra.scanner.v1.ScannerCallback
import com.simprints.fingerprint.scanner.domain.AcquireImageResponse
import com.simprints.fingerprint.scanner.domain.BatteryInfo
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.safe.NoFingerDetectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerLowBatteryException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerOperationInterruptedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnavailableVero2Feature
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnavailableVero2FeatureException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.simprints.fingerprint.infra.scanner.v1.ButtonListener as ScannerTriggerListenerV1
import com.simprints.fingerprint.infra.scanner.v1.Scanner as ScannerV1


/**
 * This class is a wrapper over the Vero 1 Scanner, that stands as a middle man between higher
 * dependencies and the vero 1 scanner object. It translates the high-level requests of high-level
 * dependencies, into low-level commands of the scanner.
 *
 * @param scannerV1  the vero 1 scanner object
 *
 */
class ScannerWrapperV1(
    private val scannerV1: ScannerV1,
    private val ioDispatcher: CoroutineDispatcher,
) : ScannerWrapper {


    /**
     * This function retrieves the unified versions of STM & un20 and Unknown for th cypress because,
     * the vero 1 doesn't run custom cypress firmware.
     */
    override fun versionInformation(): ScannerVersion =
        ScannerVersion(
            hardwareVersion = "",
            generation = ScannerGeneration.VERO_1,
            firmware = ScannerFirmwareVersions(
                cypress = ScannerFirmwareVersions.UNKNOWN_VERSION,
                stm = scannerV1.ucVersion.toInt().toString(),
                un20 = scannerV1.unVersion.toInt().toString()
            )
        )

    override fun batteryInformation(): BatteryInfo = BatteryInfo.UNKNOWN

    //Vero 1 scanners doesn't support image transfer
    override fun isImageTransferSupported(): Boolean = false

    override suspend fun connect() = withContext(ioDispatcher) {
        suspendCoroutine { cont ->
            scannerV1.connect(ScannerCallbackWrapper(
                success = {
                    cont.resume(Unit)
                },
                failure = { scannerError ->
                    scannerError?.let {
                        val fingerprintException = when (scannerError) {
                            BLUETOOTH_DISABLED -> BluetoothNotEnabledException()
                            BLUETOOTH_NOT_SUPPORTED -> BluetoothNotSupportedException()
                            SCANNER_UNBONDED -> ScannerNotPairedException()
                            BUSY, IO_ERROR -> ScannerDisconnectedException()
                            else -> UnknownScannerIssueException.forScannerError(scannerError)
                        }

                        cont.resumeWithException(fingerprintException)
                    } ?: cont.resume(Unit)
                }
            ))
        }
    }

    /**
     * This function does nothing because vero 1 scanner doesn't support firmware updates
     */
    override suspend fun setScannerInfoAndCheckAvailableOta() = withContext(ioDispatcher) {
        //Not implemented
    }


    override suspend fun disconnect() = withContext(ioDispatcher) {
        suspendCoroutine { cont ->
            scannerV1.disconnect(ScannerCallbackWrapper(
                success = { cont.resume(Unit) },
                failure = { cont.resume(Unit) }
            ))
        }
    }

    override suspend fun sensorWakeUp() = withContext(ioDispatcher) {
        suspendCoroutine { cont ->
            scannerV1.un20Wakeup(ScannerCallbackWrapper(
                success = {
                    cont.resume(Unit)
                },
                failure = { scannerError ->
                    scannerError?.let {
                        val fingerprintException = when (scannerError) {
                            UN20_LOW_VOLTAGE -> ScannerLowBatteryException()
                            else -> UnknownScannerIssueException.forScannerError(scannerError)
                        }

                        cont.resumeWithException(fingerprintException)
                    } ?: cont.resume(Unit)
                }
            ))
        }
    }

    override suspend fun sensorShutDown() = withContext(ioDispatcher) {
        suspendCoroutine { cont ->
            scannerV1.un20Shutdown(ScannerCallbackWrapper(
                success = { cont.resume(Unit) },
                failure = { cont.resumeWithException(UnknownScannerIssueException.forScannerError(it)) }
            ))
        }
    }

    override suspend fun startLiveFeedback() =
        throw UnavailableVero2FeatureException(UnavailableVero2Feature.LIVE_FEEDBACK)


    override suspend fun stopLiveFeedback() =
        throw UnavailableVero2FeatureException(UnavailableVero2Feature.LIVE_FEEDBACK)

    override fun isLiveFeedbackAvailable(): Boolean = false

    override suspend fun captureFingerprint(
        captureFingerprintStrategy: CaptureFingerprintStrategy?,
        timeOutMs: Int,
        qualityThreshold: Int,
    )  = withContext(ioDispatcher) {
        suspendCancellableCoroutine { cont ->
            scannerV1.startContinuousCapture(qualityThreshold, timeOutMs.toLong(), continuousCaptureCallback(qualityThreshold, cont))

            cont.invokeOnCancellation {
                scannerV1.stopContinuousCapture()
            }
        }
    }

    private fun continuousCaptureCallback(qualityThreshold: Int, cont: Continuation<CaptureFingerprintResponse>) =
        ScannerCallbackWrapper(
            success = {
                cont.resume(
                    CaptureFingerprintResponse(
                        scannerV1.template!!,
                        templateFormat,
                        scannerV1.imageQuality
                    )
                )
            },
            failure = {
                if (it == TIMEOUT)
                    scannerV1.forceCapture(qualityThreshold, forceCaptureCallback(cont))
                else handleFingerprintCaptureError(it, cont)
            }
        )

    private fun forceCaptureCallback(cont: Continuation<CaptureFingerprintResponse>) =
        ScannerCallbackWrapper(
            success = {
                cont.resume(
                    CaptureFingerprintResponse(
                        scannerV1.template!!,
                        templateFormat,
                        scannerV1.imageQuality
                    )
                )
            },
            failure = {
                handleFingerprintCaptureError(it, cont)
            }
        )

    private fun handleFingerprintCaptureError(error: SCANNER_ERROR?, cont: Continuation<CaptureFingerprintResponse>) {
        when (error) {
            UN20_SDK_ERROR -> cont.resumeWithException(NoFingerDetectedException()) // If no finger is detected on the sensor
            INVALID_STATE, SCANNER_UNREACHABLE, UN20_INVALID_STATE, OUTDATED_SCANNER_INFO, IO_ERROR -> cont.resumeWithException(ScannerDisconnectedException())
            BUSY, INTERRUPTED, TIMEOUT -> cont.resumeWithException(ScannerOperationInterruptedException())
            else -> cont.resumeWithException(UnexpectedScannerException.forScannerError(error, "ScannerWrapperV1"))
        }
    }

    override suspend fun acquireImage(saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy?): AcquireImageResponse =
        throw UnavailableVero2FeatureException(UnavailableVero2Feature.IMAGE_ACQUISITION)

    override suspend fun setUiIdle()  = withContext(ioDispatcher) {
        suspendCoroutine { cont ->
            scannerV1.resetUI(ScannerCallbackWrapper(
                success = {
                    cont.resume(Unit)
                },
                failure = {
                    // not sure if the comment is still relevant.
                    cont.resume(Unit)  // This call on Vero 1 can sometimes be buggy
                }
            ))
        }
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

    override fun performCypressOta(firmwareVersion: String): Flow<CypressOtaStep> =
        throw UnavailableVero2FeatureException(UnavailableVero2Feature.OTA)

    override fun performStmOta(firmwareVersion: String): Flow<StmOtaStep> =
        throw UnavailableVero2FeatureException(UnavailableVero2Feature.OTA)

    override fun performUn20Ota(firmwareVersion: String): Flow<Un20OtaStep> =
        throw UnavailableVero2FeatureException(UnavailableVero2Feature.OTA)

    private class ScannerCallbackWrapper(val success: () -> Unit, val failure: (scannerError: SCANNER_ERROR?) -> Unit) : ScannerCallback {
        override fun onSuccess() {
            success()
        }

        override fun onFailure(error: SCANNER_ERROR?) {
            failure(error)
        }
    }
}
