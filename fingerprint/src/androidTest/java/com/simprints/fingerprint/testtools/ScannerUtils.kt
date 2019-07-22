package com.simprints.fingerprint.testtools

import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprintscanner.SCANNER_ERROR
import com.simprints.fingerprintscanner.Scanner
import com.simprints.fingerprintscanner.ScannerCallback
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscannermock.MockScannerManager
import timber.log.Timber

object ScannerUtils {

    fun setupScannerForCollectingFingerprints(bluetoothAdapter: BluetoothComponentAdapter, scannerManager: ScannerManager) {
        scannerManager.scanner = Scanner(MockScannerManager.DEFAULT_MAC_ADDRESS, bluetoothAdapter)

        scannerManager.start().blockingAwait()
    }
}
