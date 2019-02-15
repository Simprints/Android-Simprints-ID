package com.simprints.id.testtools

import com.simprints.id.scanner.ScannerManager
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.Scanner
import com.simprints.libscanner.ScannerCallback
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter
import timber.log.Timber

object ScannerUtils {

    fun setupScannerForCollectingFingerprints(bluetoothAdapter: BluetoothComponentAdapter, scannerManager: ScannerManager) {
        scannerManager.scanner = Scanner("F0:AC:D7:C8:CB:22", bluetoothAdapter)

        scannerManager.scanner?.connect( object : ScannerCallback {

            override fun onSuccess() {
                scannerManager.scanner?.un20Wakeup(object : ScannerCallback {
                    override fun onSuccess() {
                        Timber.d("Test", "Scanner success un20Wakeup")
                    }
                    override fun onFailure(error: SCANNER_ERROR?) {
                        Timber.d("Test", "Scanner onFailure: $error")
                    }
                })
            }

            override fun onFailure(error: SCANNER_ERROR?) {
                Timber.d("Test", "Scanner onFailure: $error")
            }
        })
    }
}
