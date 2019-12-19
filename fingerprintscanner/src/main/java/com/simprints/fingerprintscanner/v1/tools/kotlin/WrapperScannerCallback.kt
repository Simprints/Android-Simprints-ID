package com.simprints.fingerprintscanner.v1.tools.kotlin

import com.simprints.fingerprintscanner.v1.SCANNER_ERROR
import com.simprints.fingerprintscanner.v1.ScannerCallback

class WrapperScannerCallback(val success: () -> Unit,
                             val failure: (scannerError: SCANNER_ERROR?) -> Unit) : ScannerCallback {
    override fun onSuccess() {
        success()
    }

    override fun onFailure(error: SCANNER_ERROR?) {
        failure(error)
    }
}
