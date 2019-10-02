package com.simprints.fingerprint.scanner

import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.scanner.exceptions.safe.MultipleScannersPairedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerLowBatteryException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprintscanner.api.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscanner.v1.ScannerUtils
import io.reactivex.Completable

class ScannerManagerImpl(private val bluetoothAdapter: BluetoothComponentAdapter,
                         private val scannerFactory: ScannerFactory) : ScannerManager {

    override lateinit var scanner: ScannerWrapper
    override lateinit var lastPairedScannerId: String
    override lateinit var lastPairedMacAddress: String

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

    override fun checkBluetoothStatus(): Completable = Completable.create {
        if (!bluetoothIsEnabled()) {
            it.onError(BluetoothNotEnabledException())
        } else {
            it.onComplete()
        }
    }

    private fun bluetoothIsEnabled() = ScannerUtils.isBluetoothEnabled(bluetoothAdapter)

    override fun getAlertType(it: Throwable): FingerprintAlert =
        when (it) {
            is BluetoothNotEnabledException -> FingerprintAlert.BLUETOOTH_NOT_ENABLED
            is BluetoothNotSupportedException -> FingerprintAlert.BLUETOOTH_NOT_SUPPORTED
            is MultipleScannersPairedException -> FingerprintAlert.MULTIPLE_PAIRED_SCANNERS
            is ScannerLowBatteryException -> FingerprintAlert.LOW_BATTERY
            is ScannerNotPairedException -> FingerprintAlert.NOT_PAIRED
            is UnknownScannerIssueException -> FingerprintAlert.DISCONNECTED
            else -> FingerprintAlert.UNEXPECTED_ERROR
        }
}
