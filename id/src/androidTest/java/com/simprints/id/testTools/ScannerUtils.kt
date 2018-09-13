package com.simprints.id.testTools

import com.simprints.id.tools.AppState
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.Scanner
import com.simprints.libscanner.ScannerCallback
import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter

object ScannerUtils {

    fun setupScannerForCollectingFingerprints(bluetoothAdapter: BluetoothComponentAdapter, appState: AppState) {
        appState.scanner = Scanner("F0:AC:D7:C8:CB:22", bluetoothAdapter)

        appState.scanner.connect( object : ScannerCallback {

            override fun onSuccess() {
                appState.scanner.un20Wakeup(object : ScannerCallback {
                    override fun onSuccess() {
                        Log.d("Test", "Scanner success un20Wakeup")
                    }
                    override fun onFailure(error: SCANNER_ERROR?) {
                        Log.d("Test", "Scanner onFailure: $error")
                    }
                })
            }

            override fun onFailure(error: SCANNER_ERROR?) {
                Log.d("Test", "Scanner onFailure: $error")
            }
        })
    }
}
