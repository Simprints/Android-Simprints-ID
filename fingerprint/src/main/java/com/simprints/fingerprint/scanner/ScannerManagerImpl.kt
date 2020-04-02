package com.simprints.fingerprint.scanner

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.scanner.exceptions.safe.*
import com.simprints.fingerprint.scanner.exceptions.unexpected.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.NullScannerException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import io.reactivex.Completable
import io.reactivex.Single

class ScannerManagerImpl(private val bluetoothAdapter: ComponentBluetoothAdapter,
                         private val scannerFactory: ScannerFactory,
                         private val pairingManager: ScannerPairingManager) : ScannerManager {

    override var scanner: ScannerWrapper? = null
    override var lastPairedScannerId: String? = null
    override var lastPairedMacAddress: String? = null

    override fun <T> onScanner(method: ScannerWrapper.() -> T): T =
        delegateToScannerOrThrow(method)

    override fun scanner(method: ScannerWrapper.() -> Completable): Completable =
        Completable.defer { delegateToScannerOrThrow(method) }

    override fun <T> scanner(method: ScannerWrapper.() -> Single<T>): Single<T> =
        Single.defer { delegateToScannerOrThrow(method)}

    private fun <T> delegateToScannerOrThrow(method: ScannerWrapper.() -> T) =
        scanner?.method() ?: throw NullScannerException()

    override fun initScanner(): Completable = Completable.create {
        val pairedScanners = pairingManager.getPairedScannerAddresses()
        when {
            pairedScanners.isEmpty() -> it.onError(ScannerNotPairedException())
            pairedScanners.size > 1 -> it.onError(MultipleScannersPairedException())
            else -> {
                val macAddress = pairedScanners[0]
                scanner = scannerFactory.create(macAddress)
                lastPairedMacAddress = macAddress
                lastPairedScannerId = pairingManager.convertAddressToSerialNumber(macAddress)
                it.onComplete()
            }
        }
    }

    override fun checkBluetoothStatus(): Completable = Completable.create {
        if (!bluetoothIsEnabled()) {
            it.onError(BluetoothNotEnabledException())
        } else {
            it.onComplete()
        }
    }

    private fun bluetoothIsEnabled() = bluetoothAdapter.isEnabled()

    override fun getAlertType(e: Throwable): FingerprintAlert =
        when (e) {
            is BluetoothNotEnabledException -> FingerprintAlert.BLUETOOTH_NOT_ENABLED
            is BluetoothNotSupportedException -> FingerprintAlert.BLUETOOTH_NOT_SUPPORTED
            is MultipleScannersPairedException -> FingerprintAlert.MULTIPLE_PAIRED_SCANNERS
            is ScannerLowBatteryException -> FingerprintAlert.LOW_BATTERY
            is ScannerNotPairedException -> FingerprintAlert.NOT_PAIRED
            is ScannerDisconnectedException -> FingerprintAlert.DISCONNECTED
            is UnknownScannerIssueException -> FingerprintAlert.DISCONNECTED
            else -> FingerprintAlert.UNEXPECTED_ERROR
        }
}
