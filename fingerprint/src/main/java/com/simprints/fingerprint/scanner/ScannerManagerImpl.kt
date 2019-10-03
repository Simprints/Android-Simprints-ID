package com.simprints.fingerprint.scanner

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.scanner.domain.CaptureFingerprintResponse
import com.simprints.fingerprint.scanner.domain.ScannerTriggerListener
import com.simprints.fingerprint.scanner.domain.ScannerVersionInformation
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.MultipleScannersPairedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerLowBatteryException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.NullScannerException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprintscanner.api.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscanner.v1.ScannerUtils
import io.reactivex.Completable
import io.reactivex.Single

class ScannerManagerImpl(private val bluetoothAdapter: BluetoothComponentAdapter,
                         private val scannerFactory: ScannerFactory) : ScannerManager {

    override lateinit var scanner: ScannerWrapper
    override var lastPairedScannerId: String? = null
    override var lastPairedMacAddress: String? = null

    override fun initScanner(): Completable = Completable.create {
        val pairedScanners = ScannerUtils.getPairedScanners(bluetoothAdapter)
        when {
            pairedScanners.isEmpty() -> it.onError(ScannerNotPairedException())
            pairedScanners.size > 1 -> it.onError(MultipleScannersPairedException())
            else -> {
                val macAddress = pairedScanners[0]
                scanner = scannerFactory.create(macAddress)
                lastPairedMacAddress = macAddress
                lastPairedScannerId = ScannerUtils.convertAddressToSerial(macAddress)
                it.onComplete()
            }
        }
    }

    override fun connect(): Completable = delegateCompletableToScannerOrError { connect() }

    override fun disconnectIfNecessary(): Completable =
        if (::scanner.isInitialized) {
            scanner.disconnect()
        } else {
            Completable.complete()
        }

    override fun sensorWakeUp(): Completable = delegateCompletableToScannerOrError { sensorWakeUp() }

    override fun sensorShutDown(): Completable = delegateCompletableToScannerOrError{ sensorShutDown() }

    override fun captureFingerprint(timeOutMs: Int, qualityThreshold: Int): Single<CaptureFingerprintResponse> =
        delegateSingleToScannerOrError { captureFingerprint(timeOutMs, qualityThreshold) }

    override fun setUiIdle(): Completable = delegateCompletableToScannerOrError { setUiIdle() }

    override fun registerTriggerListener(triggerListener: ScannerTriggerListener) =
        delegateToScannerOrThrow { registerTriggerListener(triggerListener) }

    override fun unregisterTriggerListener(triggerListener: ScannerTriggerListener) =
        delegateToScannerOrThrow { unregisterTriggerListener(triggerListener) }

    override val versionInformation: ScannerVersionInformation
        get() = delegateToScannerOrThrow { versionInformation }

    private fun <T> delegateToScannerOrThrow(method: ScannerWrapper.() -> T) =
        if (::scanner.isInitialized) {
            scanner.method()
        } else {
            throw NullScannerException()
        }

    private fun delegateCompletableToScannerOrError(method: ScannerWrapper.() -> Completable) =
        Completable.defer { delegateToScannerOrThrow(method) }

    private fun <T> delegateSingleToScannerOrError(method: ScannerWrapper.() -> Single<T>) =
        Single.defer { delegateToScannerOrThrow(method)}

    override fun checkBluetoothStatus(): Completable = Completable.create {
        if (!bluetoothIsEnabled()) {
            it.onError(BluetoothNotEnabledException())
        } else {
            it.onComplete()
        }
    }

    private fun bluetoothIsEnabled() = ScannerUtils.isBluetoothEnabled(bluetoothAdapter)

    override fun getAlertType(e: Throwable): FingerprintAlert =
        when (e) {
            is BluetoothNotEnabledException -> FingerprintAlert.BLUETOOTH_NOT_ENABLED
            is BluetoothNotSupportedException -> FingerprintAlert.BLUETOOTH_NOT_SUPPORTED
            is MultipleScannersPairedException -> FingerprintAlert.MULTIPLE_PAIRED_SCANNERS
            is ScannerLowBatteryException -> FingerprintAlert.LOW_BATTERY
            is ScannerNotPairedException -> FingerprintAlert.NOT_PAIRED
            is UnknownScannerIssueException -> FingerprintAlert.DISCONNECTED
            else -> FingerprintAlert.UNEXPECTED_ERROR
        }
}
