package com.simprints.fingerprintscanner.tools.kotlin

import com.simprints.fingerprintscanner.SCANNER_ERROR
import com.simprints.fingerprintscanner.ScannerCallback

class WrapperScannerCallback(val success: () -> Unit,
                             val failure: (scannerError: SCANNER_ERROR?) -> Unit) : ScannerCallback {
    override fun onSuccess() {
        success()
    }

    override fun onFailure(error: SCANNER_ERROR?) {
        failure(error)
    }
}
