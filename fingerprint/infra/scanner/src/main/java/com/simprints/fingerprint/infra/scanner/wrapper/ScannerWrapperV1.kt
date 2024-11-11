package com.simprints.fingerprint.infra.scanner.wrapper

import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperV1.ScannerCallbackWrapper
import com.simprints.fingerprint.infra.scanner.domain.BatteryInfo
import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.infra.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerLowBatteryException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnavailableVero2Feature
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnavailableVero2FeatureException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.BLUETOOTH_DISABLED
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.BLUETOOTH_NOT_SUPPORTED
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.BUSY
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.IO_ERROR
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.SCANNER_UNBONDED
import com.simprints.fingerprint.infra.scanner.v1.SCANNER_ERROR.UN20_LOW_VOLTAGE
import com.simprints.infra.config.store.models.FingerprintConfiguration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
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
internal class ScannerWrapperV1(
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
    override suspend fun setScannerInfoAndCheckAvailableOta(fingerprintSdk: FingerprintConfiguration.BioSdk) =
        withContext(ioDispatcher) {
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

    override fun isConnected() = scannerV1.isConnected

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

    override suspend fun turnOffSmileLeds() = withContext(ioDispatcher) {
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
    override suspend fun turnOnFlashingWhiteSmileLeds(){
        // While LED manipulation is supported in Vero1, it is not currently required.
    }

    override suspend fun setUiBadCapture() {
        // While LED manipulation is supported in Vero1, it is not currently required.
    }

    override suspend fun setUiGoodCapture() {
        // While LED manipulation is supported in Vero1, it is not currently required.
    }
}
